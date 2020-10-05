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

import io.github.bkosaraju.utils.aws.AwsUtils
import io.github.bkosaraju.utils.jsh.SftpUtils
import io.github.bkosaraju.utils.databricks.RunSparkJobInDBC
import io.github.bkosaraju.utils.kubernetes.RunSparkJobInKube
import io.github.bkosaraju.utils.aws.emr.RunSparkJobInEMR
import io.github.bkosaraju.utils.common.MaskSensitiveValuesFromMap
import io.github.bkosaraju.pipeline.AppFunctions

class ActionLauncher extends AppFunctions {

  def actionLauncher(config: collection.mutable.Map[String,String]): Unit = {
    try {
      if (config.nonEmpty) {
        logger.info("Identified following configurations for execution..")
        logger.info("ConfigKey".formatted(s"%40s") + " : " + "ConfigValue".formatted(s"%110s") )
        collection.mutable.LinkedHashMap(MaskSensitiveValuesFromMap(config.toMap).toSeq.sortBy(_._1):_*)
          .foreach { x =>
            logger.info(x._1.formatted(s"%40s") + " : " + x._2.formatted(s"%110s") ) }
      }
      config.getOrElse("taskType","").toLowerCase match {
        case "sftp" => (new SftpUtils).fetchSftpFile(config.toMap)
        case "shell" => runShell(config.toMap)
        case "s3copy" => (new AwsUtils).copyS3Object(config.toMap)
        case "s3push" => (new AwsUtils).putS3Object(config.toMap,config("fileName"),config("keyName"))
        case "spark_on_databricks" => RunSparkJobInDBC(config.toMap)
        case "spark_on_kubernetes" => RunSparkJobInKube(config.toMap)
        case "spark_on_emr" => RunSparkJobInEMR(config.toMap)
        case _ => {
          logger.error(s"Unknown Task Type(${config.getOrElse("taskType","")}) Specified")
          UnknownProcessTypeException(s"Unknown Task Type(${config.getOrElse("taskType","")}) Specified")
        }
      }
    } catch {
      case e: Exception => {
        logger.error(s"Error Occurred while running the process(${config.getOrElse("taskType","")})", e)
        throw e
      }
    }
  }
}

object ActionLauncher {
  def apply(config: collection.mutable.Map[String,String]) : Unit = {
    (new ActionLauncher).actionLauncher(config)
  }
}
