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

package io.github.bkosaraju.pipeline.prometheus.functions

import io.github.bkosaraju.utils.common.Session
import io.prometheus.client.Collector
import io.prometheus.client.exporter.{BasicAuthHttpConnectionFactory, PushGateway}

import scala.collection.JavaConverters._

class PushMetrics extends Session {
  def pushMetrics(config: Map[String,String],collector : Collector): Unit = {
    try {
      val metricsGateway = config.getOrElse("pushGatewayEndPoint","127.0.0.1:9091")
      val pushGateway = new PushGateway(metricsGateway)
      if(config.getOrElse("pushGatewayUser","").nonEmpty && config.getOrElse("pushGatewayPassword","").nonEmpty) {
        logger.info(s"configured pushGateway with basic authentication - on ${metricsGateway}@${config("pushGatewayUser")}")
        pushGateway.setConnectionFactory(new BasicAuthHttpConnectionFactory(config("pushGatewayUser"),config("pushGatewayUser")))
      } else {
        logger.info(s"configured pushGateway without any authentication - on ${metricsGateway}")
      }
      val groupingKeys = collection.mutable.Map[String,String]()
      groupingKeys.put("jobId",config.getOrElse("jobId","NaN"))
      groupingKeys.put("jobExecutionId",config.getOrElse("jobExecutionId","NaN"))
      if (config.getOrElse("actionType","").equals("TASK")) {
        groupingKeys.put("taskId", config.getOrElse("taskId", "NaN"))
        groupingKeys.put("taskExecutionId", config.getOrElse("taskExecutionId", "NaN"))
      }
      logger.info(s"tagging the metrics with $groupingKeys")
      pushGateway.pushAdd(collector,s"pipeline_application",groupingKeys.asJava)
    }
    catch {
      case e : Exception => {
        logger.warn("Unable to push metrics to Push gateway",e)
        //throw e
      }
    }
  }
}

object PushMetrics {
  def apply(config: Map[String,String],collector: Collector): Unit = {
    (new PushMetrics()).pushMetrics(config,collector)
  }
}
