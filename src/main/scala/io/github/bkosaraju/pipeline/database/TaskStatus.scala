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
import io.github.bkosaraju.pipeline.functions.Config
import org.jdbi.v3.core.Jdbi

import scala.beans.BeanProperty
import collection.JavaConverters._
import scala.collection.mutable

class TaskStatus extends JdbcHelper with Session with Config {
  @BeanProperty
  var config: mutable.Map[String, String] = collection.mutable.Map[String, String]()

  @BeanProperty
  var jdbi: Jdbi = _

  def updateTaskExecution(): collection.mutable.Map[String, String] = {
    try {
      if (!config.contains("taskExecutionId")) {
        val taskInitSql = jdbi.getresource(INIT_TASK_SQL)
        config.put("taskExecutionStatus", "START")
        config.put("taskExecutionEndTimestamp",null)
        using(jdbi.open()) { handle =>
          config.put("taskExecutionId",
            handle.createQuery(taskInitSql)
              .bindMap(config.asJava)
              .mapTo(classOf[java.lang.Long])
              .first().toString
          )
        }
        logger.info(s"Initialized task(START) with task_execution_id : ${config("taskExecutionId")}")
      } else {
        logger.info(s"Updating task status in audit table with ${config("taskExecutionStatus")} for taskId : ${config("taskId")}, jobExecutionId :${config("jobExecutionId")}")
        val updateTaskStatusSql = jdbi.getresource(UPDATE_TASK_SQL)
        using(jdbi.open()) { handle =>
            handle.createUpdate(updateTaskStatusSql)
              .bindMap(config.asJava)
              .execute()
          }
        config.remove("taskExecutionId")
      }
      config
    } catch {
      case e: Exception => {
        logger.error(s"Unable to start/update the task (${config.getOrElse("taskId", "NA")})..", e)
        throw e
      }
    }
  }
}

object TaskStatus {
  def apply(config: collection.mutable.Map[String,String]): collection.mutable.Map[String,String] = {
    TaskStatus(config,DbConnection(config.toMap))
  }

  def apply(config: collection.mutable.Map[String,String], jdbi: Jdbi): collection.mutable.Map[String,String] = {
    val taskStatus = new TaskStatus()
    taskStatus.setConfig(config)
    taskStatus.setJdbi(jdbi)
    taskStatus.updateTaskExecution()
  }
}

