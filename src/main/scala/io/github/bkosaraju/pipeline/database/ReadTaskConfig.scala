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

import io.github.bkosaraju.utils.aws.AwsUtils
import io.github.bkosaraju.utils.common.Session
import io.github.bkosaraju.pipeline.functions.{Config, Exceptions}
import io.github.bkosaraju.pipeline.model.TaskConfig
import org.jdbi.v3.core.Jdbi

import scala.collection.JavaConverters._
import scala.beans.BeanProperty

class ReadTaskConfig extends JdbcHelper with Session with Exceptions with Config{

  @BeanProperty
  var config = Map[String,String]()

  @BeanProperty
  var jdbi: Jdbi = _

  def getTaskConfig: Seq[TaskConfig] = {
   try {
    val taskConfigSql = jdbi.getresource(TASK_CONFIG_SQL)
    var taskConfiguration = List[TaskConfig]()

    using(jdbi.open()) { handle =>
      taskConfiguration =
        handle
          .createQuery(taskConfigSql)
          .bindMap(config.asJava)
          .mapToBean(classOf[TaskConfig])
          .list.asScala.toList
    }

    //retrive SSM value
    val awsUtils = new AwsUtils()
     awsUtils.setConfig(config)
    val taskConfigData = taskConfiguration.map(cfgItem => {
      if (cfgItem.configType.equalsIgnoreCase("ssm")) {
        logger.info(s"extracting secret for ${cfgItem.configValue} from SSM")
        cfgItem.setConfigValue(awsUtils.getSSMValue(cfgItem.getConfigValue))
        cfgItem
      } else {
        cfgItem
      }
    }
    )
    taskConfigData
  } catch {
        case e: Exception => {
          logger.error("Unable to Read Data from metadata(databse / pramater store(awsSSM) )", e)
          throw MetaDataReadException("Unable to Read Data from metadata store")
        }
      }
    }
}

object ReadTaskConfig {
  def apply(config: Map[String,String]): Seq[TaskConfig] = {
    ReadTaskConfig(config,DbConnection(config))
  }

  def apply(config: Map[String,String],jdbi: Jdbi ): Seq[TaskConfig] = {
    val readTaskConfig = new ReadTaskConfig()
    readTaskConfig.setConfig(config)
    readTaskConfig.setJdbi(jdbi)
    readTaskConfig.getTaskConfig
  }
}

