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
import org.jdbi.v3.core.Jdbi

import scala.collection.JavaConverters._
import scala.beans.BeanProperty

class JobOrderValidation extends JdbcHelper with Session with Exceptions with Config {

  @BeanProperty
  var config = Map[String, String]()

  @BeanProperty
  var jdbi: Jdbi = _

  def validateJobOrder : Boolean = {
    try {
      val validationSql = jdbi.getresource(JOB_ORDER_VALIDATION_SQL)
      var vResults = collection.mutable.Buffer(Map[java.lang.String, AnyRef]())
      using(jdbi.open()) { handle =>
        vResults = handle
          .createQuery(validationSql)
          .bindMap(config.asJava)
          .mapToMap()
          .list
          .asScala.map(_.asScala).map(x => x.toMap)
      }

      if (vResults.nonEmpty) {
        throw NewerOrderDateFoundForJob(s"there already an instance for job: ${config.getOrElse("jobId", "NA")}, is found for newer order date ${vResults.head.getOrElse("jobOrderTimestamp", "NA")} with job_execution_id :${vResults.head.getOrElse("jobExecutionId", "NA")} ")
      } else {
        logger.info(s"job order date validated for :${config("jobOrderTimestamp")}")
        true
      }
    } catch {
      case e: Exception => {
        logger.error("Unable to validate job order")
        throw e
      }
    }
  }
}

object JobOrderValidation {

  def apply(config: Map[String, String]): Boolean = {
    JobOrderValidation(config,DbConnection(config))
  }

  def apply (config: Map[String, String], jdbi: Jdbi) : Boolean = {
    val jOrderValidatin = new JobOrderValidation()
    jOrderValidatin.setConfig(config)
    jOrderValidatin.setJdbi(jdbi)
    jOrderValidatin.validateJobOrder
  }
}




