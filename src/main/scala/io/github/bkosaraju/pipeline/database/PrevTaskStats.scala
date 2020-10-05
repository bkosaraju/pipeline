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

import java.sql.ResultSet
import java.util.function.Consumer

import io.github.bkosaraju.utils.common.Session
import io.github.bkosaraju.pipeline.functions.{Config, Exceptions}
import io.github.bkosaraju.pipeline.model.PreviousTaskConfig
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.{CaseStrategy, MapMapper, MapMappers, Mappers, RowMappers}
import org.apache.commons.beanutils.BeanMap

import collection.JavaConverters._
import scala.beans.BeanProperty
import scala.collection.mutable

class PrevTaskStats
  extends JdbcHelper
    with Session
    with Exceptions
    with Config
    {

  @BeanProperty
  var config: mutable.Map[String, String] = collection.mutable.Map[String, String]()

  @BeanProperty
  var jdbi: Jdbi = _

  def loadPrevTaskConfig(): Unit = {
    try {
      val prevTaskStatusSQL = jdbi.getresource(PREV_TASK_STATS_SQL)
      var prevTaskStats = Map[AnyRef,AnyRef]()
      using(jdbi.open()) { handle =>
        prevTaskStats =
          handle
            .createQuery(prevTaskStatusSQL)
            .bindMap(config.asJava)
            .mapToBean(classOf[PreviousTaskConfig])
            .list.asScala.toList.flatMap(x => (new BeanMap(x)).asScala.map(y => Map(y._1 -> y._2))).flatten.toMap
      }
      prevTaskStats.map(x => if (Option(x._2).isDefined) {
        config.put(x._1.toString, x._2.toString)
      }
      )
    } catch {
      case e: Exception => {
        logger.error("Unable to read previous job status configuration ")
        throw e
      }
    }
  }
}


object PrevTaskStats {
  def apply(config: collection.mutable.Map[String, String], jdbi: Jdbi): Unit = {
    val prevTasksStats = new PrevTaskStats()
    prevTasksStats.setConfig(config)
    prevTasksStats.setJdbi(jdbi)
    prevTasksStats.loadPrevTaskConfig()
  }

  def apply(config: collection.mutable.Map[String, String]): Unit = {
    PreviousStatus(config, DbConnection(config.toMap))
  }
}
