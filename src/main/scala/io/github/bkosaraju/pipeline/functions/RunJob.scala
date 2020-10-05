/*
 *   Copyright (C) 2019-2020 bkosaraju
 *   All Rights Reserved.
 *
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package io.github.bkosaraju.pipeline.functions

import io.github.bkosaraju.pipeline.database._
import io.github.bkosaraju.utils.common.{SendMail, Session}
import io.github.bkosaraju.utils.splunk.CreateSplunkNotification
import io.github.bkosaraju.pipeline.database.{DbConnection, GetTaskIds, JdbcHelper, JobOrderValidation, JobStatus, PrevTaskStats, PreviousStatus, ReadTaskConfig, TaskStatus}
import io.github.bkosaraju.pipeline.prometheus.functions.PipelineMetrics
import io.github.bkosaraju.pipeline.prometheus.model.GaugeMetric

import scala.beans.BeanProperty

class RunJob
  extends JdbcHelper
    with Session
    with SendMail
    with Exceptions {

  @BeanProperty
  var config: scala.collection.mutable.Map[String, String] = scala.collection.mutable.Map[String, String]()


  def runJob(config: scala.collection.mutable.Map[String, String]): Unit = {
    var appException : Exception = new Exception
    var appErrorMessage : String = ""
    val jdbi = DbConnection(config.toMap)
    config.put("dbProductName", getDBProductName(jdbi))
    var jobMetrics, taskMetrics = Option(new GaugeMetric)
    var metricInitializer = true
    var taskConfig = collection.mutable.Map[String,String]()
    try {
        PreviousStatus(config, jdbi)
      if (config("isRunnable").toBoolean) {
        JobOrderValidation(config.toMap, jdbi)
        JobStatus(config, jdbi)
        for (task <- GetTaskIds(config.toMap, jdbi)) {
          config.put("taskId", task.getTaskId.toString)
          TaskStatus(config, jdbi)
          lazy val TASK_MSG = s"job_id: ${config("jobId")}, job_execution_id: ${config("jobExecutionId")}, task_id: ${config("taskId")} task_execution_id: ${config("taskExecutionId")}"
          logger.info(s"Started Running process for $TASK_MSG")
          taskConfig = config.filter(! _._1.toLowerCase.contains("password") ) ++ Map("taskType" -> task.getTaskType)
          PrevTaskStats(taskConfig,jdbi) //loads previous successful stats.
          ReadTaskConfig(config.toMap, jdbi).map(tc => taskConfig.put(tc.getConfigKey, tc.getConfigValue))
          if (metricInitializer) {
            jobMetrics = PipelineMetrics( taskConfig.filter( x => ! Seq("taskId","taskExecutionId").contains(x._1)).toMap, "START", "JOB", None )
            metricInitializer =  false
          }
          taskMetrics = PipelineMetrics(taskConfig.toMap,"START","TASK", None)
          ActionLauncher(taskConfig)
          config.put("taskExecutionStatus", "END")
          PipelineMetrics( taskConfig.toMap, "END", "TASK", taskMetrics )
          TaskStatus(config, jdbi)
          logger.info(s"Successfully Completed Running process for  ${TASK_MSG}")
        }
        config.put("jobExecutionStatus", "END")
        PipelineMetrics(
          taskConfig.filter( x => ! Seq("taskId","taskExecutionId").contains(x._1)).toMap, "END", "JOB",
           jobMetrics)
        JobStatus(config, jdbi)
      }
    } catch {
      case nonValidOrder: NewerOrderDateFoundForJob => {
        appException = nonValidOrder
        appErrorMessage = nonValidOrder.getMessage
      }
      case e: Exception => {
        config.put("jobExecutionStatus", "FAIL")
        config.put("taskExecutionStatus", "FAIL")
        PipelineMetrics(taskConfig.toMap,"FAIL","TASK", taskMetrics)
        PipelineMetrics( taskConfig.filter( x => ! Seq("taskId","taskExecutionId").contains(x._1)).toMap, "FAIL", "JOB", jobMetrics )
        if (config.contains("taskExecutionId") && config.contains("jobExecutionId")) {
          JobStatus(config, jdbi)
          TaskStatus(config, jdbi)
        } else if (config.contains("jobExecutionId")) {
          JobStatus(config, jdbi)
        }
        logger.error(s"Exception Occurred while processing job (${config("jobId")}) ..")
        appException = e
        appErrorMessage = e.getMessage
      }
    }

    if ( appErrorMessage.nonEmpty ) {
      if (Seq("yes","true").contains(taskConfig.getOrElse("createSplunkNotification","false").toLowerCase)) {
        try {
          logger.info("Sending alert to Splunk")
          CreateSplunkNotification(taskConfig.toMap ++ Map("errorMessage" -> appException.getMessage, "errorStackTrace" -> appException.getStackTrace.mkString("\n")))
        } catch {
          case e: Exception => {
            logger.warn("Unable to send Alert to Splunk", e)
          }
        }
      }
      if (Seq("yes","true").contains(taskConfig.getOrElse("sendMailFlag","false").toLowerCase)) {
        try {
          logger.info("Sending alert e-mail for user..")
          sendMailFromError(appException, taskConfig.toMap)
        } catch {
          case e: Exception => {
            logger.warn("Unable to send e-mail with error details", e)
          }
        }
      }
      throw appException
    }

  }
}


object RunJob {
  def apply(config : collection.mutable.Map[String,String]): Unit = {
    (new RunJob()).runJob(config)
  }
}
