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
import io.github.bkosaraju.pipeline.functions.{Config, Exceptions}
import io.github.bkosaraju.pipeline.model.JobTask

import collection.JavaConverters._
import org.jdbi.v3.core.Jdbi

import scala.beans.BeanProperty

class GetTaskIds() extends JdbcHelper with Session with Exceptions with Config {

  @BeanProperty
  var config = Map[String, String]()

  @BeanProperty
  var jdbi : Jdbi = _

  def getTaskIds : List[JobTask] = {

    val jobTaskSql = jdbi.getresource(JOB_TASK_SQL)
    try {
      var taskList =  List[JobTask]()
        using(jdbi.open()) {
          handle =>
            taskList =
            handle.createQuery(jobTaskSql)
            .bindMap(config.asJava)
            .mapToBean(classOf[JobTask]).list().asScala.toList
        }

      if (taskList.nonEmpty) {
          logger.info("Identified following tasks for execution..")
        taskList.foreach { x =>
          logger.info("task_id: " + x.getTaskId +
            ", task_name: " + x.getTaskName +
            ", task_seq_id: " + x.getTaskSeqId)
        }
        taskList
      } else {
        logger.error("No Valid tasks found for for execution using the query, might be due no active taskss defined in job_task_order or all tasks have END status in task_execution")
        logger.error(jobTaskSql + s" substituted values [${config.getOrElse("jobExecutionId","")}, ${config.getOrElse("jobId","")}")
        throw NoValidTasksFound("No Valid tasks found for for execution")
      }
    } catch {
      case e: NoValidTasksFound => throw e
      case e: Exception => {
        logger.error("Unable to Read Data from metadata store :" + jobTaskSql, e)
        throw MetaDataReadException("Unable to Read Data from metadata store")
      }
    }
  }


}

object GetTaskIds {

  def apply(config: Map[String,String]): List[JobTask] = {
    GetTaskIds(config,DbConnection(config))
  }

  def apply(config: Map[String,String], jdbi: Jdbi): List[JobTask] = {
    val getTaksIds = new GetTaskIds()
    getTaksIds.setConfig(config)
    getTaksIds.setJdbi(jdbi)
    getTaksIds.getTaskIds
  }
}

