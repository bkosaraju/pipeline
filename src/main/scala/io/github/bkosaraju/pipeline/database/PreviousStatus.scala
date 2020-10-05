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

package io.github.bkosaraju.pipeline.database

import io.github.bkosaraju.utils.common.Session
import org.jdbi.v3.core.Jdbi
import java.lang.{Boolean => BLN}

import io.github.bkosaraju.pipeline.functions.{Config, Exceptions}
import io.github.bkosaraju.pipeline.model

import collection.JavaConverters._
import scala.beans.BeanProperty

class PreviousStatus
  extends JdbcHelper
    with Exceptions
    with Config
    with Session {

  @BeanProperty
  var config: Map[String, String] = Map()

  @BeanProperty
  var jdbi: Jdbi = _

  var latestJobStatus: model.JobStatus = _

  def previousStatus: Long = {
    val jobSQL = jdbi.getresource(JOB_VALIDATION_SQL)
    var jobStatus = 0
    using(jdbi.open) { handle => {
      jobStatus =
        handle.createQuery(jobSQL)
          .bindMap(config.asJava)
          .mapTo(classOf[java.lang.Integer])
          .list().asScala.length
    }
    }
    if (jobStatus > 0) {
      logger.info("validated job using jobId : {}", config.getOrElse("jobId", "NA"))
    } else {
      logger.error("jobId : " + config.getOrElse("jobId", "NA") + "Specified JobID not existed/not existed in pipeline metadata")
      throw InvalidJobException("Specified JobID not existed/not existed in pipeline metadata")
    }

    val latestJobStatusSQL = jdbi.getresource(JOB_STATUS_SQL)


    using(jdbi.open) { handle =>
      latestJobStatus = handle.createQuery(latestJobStatusSQL)
        .bindMap(config.asJava)
        .mapToBean(classOf[model.JobStatus]).list().asScala.headOption.getOrElse(new model.JobStatus)
    }
    val cnfg = collection.mutable.Map(config.toSeq: _*)

    if (Seq("START","FAIL").contains(latestJobStatus.getJobExecutionStatus.toUpperCase)) {
      cnfg.put("jobExecutionId", latestJobStatus.getMaxExecutionId.toString)
      cnfg.put("jobId", latestJobStatus.jobId.toString)
      if (BLN.parseBoolean(config("rerunFlag"))) {
        logger.info("job requested to run as rerun mode for previous run")
        cnfg.put("jobExecutionStatus", "RERUN")
        latestJobStatus.setJobExecutionStatus("RERUN")
        JobStatus(cnfg, jdbi)
      } else if (BLN.parseBoolean(config("endRunFlag"))) {
        logger.info("job requested to run as end previous run and start a fresh run")
        cnfg.put("jobExecutionStatus", "END")
        JobStatus(cnfg, jdbi)
        latestJobStatus.setJobExecutionStatus("END")
      }
    }

    latestJobStatus.getJobExecutionStatus match {
      case START => {
        logger.error("can not proceed further due to pre-existed running instance")
        throw PreExistedRunningInstanceFound(s"There already a running instance (${latestJobStatus.getJobExecutionStatus}) with ID ${latestJobStatus.getMaxExecutionId}, job_order_timestamp: ${latestJobStatus.getJobOrderTimeStamp}")
      }
      case FAIL => {
        logger.error("can not proceed further due to pre-existed failed instance")
        throw PreExistedFailedInstanceFound(s"Found Failed instance (${latestJobStatus.getJobExecutionStatus}) with ID ${latestJobStatus.getMaxExecutionId}, job_order_timestamp: ${latestJobStatus.getJobOrderTimeStamp}")
      }
      case RERUN => {
        logger.info(s"found rerun instance for job, re-running instance ${latestJobStatus.getMaxExecutionId}")
        latestJobStatus.getMaxExecutionId
      }
      case END|FIRST_RUN => {
        logger.info(s"found a previously completed instance (${latestJobStatus.getMaxExecutionId}) with job_order_timestamp :${latestJobStatus.getJobOrderTimeStamp}, commencing new run..")
        0
      }
      case null => {
        logger.info("no previous execution instance found, starting new one")
        0
      }
      case _ => {
        throw UnknownJobStatus(s"Unknown Instance of Job execution ( ${latestJobStatus.getMaxExecutionId}) found for job_order_timestamp (${latestJobStatus.getJobOrderTimeStamp})")
      }
    }
  }
}

object PreviousStatus {
  def apply(config: Map[String, String]): Long = {
    PreviousStatus(config, DbConnection(config))
  }

  def apply(config: Map[String, String], jdbi: Jdbi): Long = {
    val validateJob = new PreviousStatus()
    validateJob.setConfig(config)
    validateJob.setJdbi(jdbi)
    validateJob.previousStatus
  }

  def apply(config: collection.mutable.Map[String, String], jdbi: Jdbi)  {
    config.put("isRunnable", "false")
    if (PreviousStatus(config.toMap, jdbi) >= 0) {
      config.put("previousExecutionId", PreviousStatus(config.toMap, jdbi).toString)
      config.put("isRunnable", "true")
    }
  }
}