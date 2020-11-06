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

import java.time.{ZoneId, ZonedDateTime}

import io.github.bkosaraju.utils.common.Session
import io.github.bkosaraju.pipeline.functions.Config
import org.jdbi.v3.core.Jdbi

import scala.beans.BeanProperty
import collection.JavaConverters._

class JobStatus extends JdbcHelper with Session with Config{
  @BeanProperty
  var config: collection.mutable.Map[String, String] = collection.mutable.Map()

  @BeanProperty
  var jdbi : Jdbi = _

  def updateJobExecution(): Unit = {
    try {
      if (!config.contains("jobExecutionId")) {
        val jobInitSql = jdbi.getresource(INIT_JOB_SQL)
        config.put("jobExecutionStatus", "START")
        config.put("jobExecutionEndTimestamp",null)
        using(jdbi.open()) { handle =>
          config.put("jobExecutionId",
            handle.createQuery(jobInitSql)
              .bindMap(config.asJava)
              .mapTo(classOf[java.lang.Long])
              .first().toString
          )
        }
        logger.info("Started Running job for jobId :" + config("jobId") + ", job_execution_id: " + config("jobExecutionId"))
      } else {
        logger.info(s"Updating job status in audit table with ${config("jobExecutionStatus")} for jobId : ${config("jobId")}, jobExecutionId :${config("jobExecutionId")}")
        val updateJobStatusSql = jdbi.getresource(UPDATE_JOB_SQL)
        using(jdbi.open()) { handle =>
            handle.createUpdate(updateJobStatusSql)
              .bindMap(config.asJava)
              .execute()
          }
        }
    } catch {
      case e: Exception => {
        logger.error(s"Unable to start/update the job (${config.getOrElse("jobId", "NA")})..", e)
        throw e
      }
    }
  }
}

object JobStatus {
  def apply(config: collection.mutable.Map[String,String]): Unit = {
    JobStatus(config,DbConnection(config.toMap))
  }

  def apply(config: collection.mutable.Map[String,String], jdbi: Jdbi): Unit = {
    val jobStatus = new JobStatus()
    jobStatus.setConfig(config)
    jobStatus.setJdbi(jdbi)
    jobStatus.updateJobExecution()
  }
}

