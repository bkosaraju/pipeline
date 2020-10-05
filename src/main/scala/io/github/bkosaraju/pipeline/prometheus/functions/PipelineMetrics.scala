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
import io.github.bkosaraju.pipeline.prometheus.model.GaugeMetric
import io.prometheus.client.CollectorRegistry

import scala.beans.BeanProperty

class PipelineMetrics extends Session{
  @BeanProperty
  var config :Map[String,String] = _

  @BeanProperty
  var status : String = _

  @BeanProperty
  var actionType : String = _

//  @BeanProperty
//  var registry : CollectorRegistry = _

  @BeanProperty
  var durationMetric : GaugeMetric = _

  def takeMetrics () : GaugeMetric = {
    actionType match {
      case "JOB" => {
            status match {
              case "START" => {
                val tags = Map("jobId"-> s"${config.getOrElse("jobId","NaN")}","jobStatus" ->"START")
                val durationTracker = new MetricsCollector
                 logger.info(s"Initializing job duration tracker pipeline_job_duration_seconds with tags ${tags}")
                durationTracker.initGaugeMetric("pipeline_job_duration_seconds", "Application Job duration tracker", tags )

              }
              case "END" => {
                durationMetric.getGaugeStartTimer.setDuration()
                val tags = Map("jobId"-> s"${config.getOrElse("jobId","NaN")}","jobStatus" ->"END")
                val endCollector = new MetricsCollector
                val endMetric = endCollector.initGaugeMetric(
                  "pipeline_job_last_completion",
                  "Application Completed time",
                  tags
                )
                endMetric.getGauge.labels(tags.values.toSeq : _*).setToCurrentTime()
                endMetric
              }
              case "FAIL" => {
                durationMetric.getGaugeStartTimer.setDuration()
                val tags = Map("jobId"-> s"${config.getOrElse("jobId","NaN")}","jobStatus" ->"END")
                val endCollector = new MetricsCollector
                val endMetric = endCollector.initGaugeMetric(
                  "pipeline_job_last_failure",
                  "Application faild time",
                  tags
                )
                endMetric.getGauge.labels(tags.values.toSeq : _*).setToCurrentTime()
                endMetric
              }
            }
  }
      case "TASK" => {
        status match {
          case "START" => {
            val tags = Map("jobId"-> s"${config.getOrElse("jobId","NaN")}","jobStatus" ->"RUNNING","taskId"->s"${config.getOrElse("taskId","NaN")}","taskStatus"->"START")
            val durationTracker = new MetricsCollector
              durationTracker.initGaugeMetric(
                "pipeline_task_duration_seconds",
                "Application task start Status",
                tags
            )
          }
          case "END" => {
            durationMetric.getGaugeStartTimer.setDuration()
            val tags = Map("jobId"-> s"${config.getOrElse("jobId","NaN")}","jobStatus" ->"RUNNING","taskId"->s"${config.getOrElse("taskId","NaN")}","taskStatus"->"END")
            val endCollector = new MetricsCollector
            val endMetric = endCollector.initGaugeMetric(
              "pipeline_task_last_completion",
              "Application task Completed time",
              tags
            )
            endMetric.getGauge.labels(tags.values.toSeq : _*).setToCurrentTime()
            endMetric

          }
          case "FAIL" => {
            durationMetric.getGaugeStartTimer.setDuration()
            val tags = Map("jobId"-> s"${config.getOrElse("jobId","NaN")}","jobStatus" ->"RUNNING","taskId"->s"${config.getOrElse("taskId","NaN")}","taskStatus"->"END")
            val endCollector = new MetricsCollector
            val endMetric = endCollector.initGaugeMetric(
              "pipeline_task_last_failure",
              "Application task faild time",
              tags
            )
            endMetric.getGauge.labels(tags.values.toSeq : _*).setToCurrentTime()
            endMetric

          }
        }
      }
      }
    }
}

object PipelineMetrics extends Session {

  def apply(
             config: Map[String, String],
             status: String,
             actionType: String,
             oMetricsCollector : Option[GaugeMetric] ) : Option[GaugeMetric] = {
    if (config.getOrElse("pushMetricsToPrometheus", "false").toBoolean) {
      logger.info(s"collecting metrics for ${actionType}:  ${
        if (actionType.equals("TASK")) {
          config.getOrElse("taskId", "NaN")
        } else config.getOrElse("jobId", "NaN")
      }")
      try {
        val mConfig : collection.mutable.Map[String,String] = collection.mutable.Map[String,String](config.toSeq : _*)
        mConfig.put("actionType",actionType)
        val metrics = new PipelineMetrics()
        metrics.setConfig(config)
        metrics.setStatus(status)
        metrics.setActionType(actionType)
        if(oMetricsCollector.isDefined) {
          metrics.setDurationMetric(oMetricsCollector.orNull)
        }
        val res = Some(metrics.takeMetrics())
        if(oMetricsCollector.isDefined) {
          PushMetrics(mConfig.toMap,oMetricsCollector.get.getGauge)
        }
        PushMetrics(mConfig.toMap,res.get.getGauge)
        if(status == "END") {
          logger.info(s"Unregistering metrics register for ${actionType} : ${
            if (actionType.equals("TASK")) {
              config.getOrElse("taskId", "NaN")
            } else {
              config.getOrElse("jobId", "NaN")
            }
          }"
          )
          CollectorRegistry.defaultRegistry.unregister(res.get.getGauge)
          if(oMetricsCollector.isDefined) {
            CollectorRegistry.defaultRegistry.unregister(oMetricsCollector.get.getGauge)
          }
        }
        res
      } catch {
        case e: Exception => {
          logger.warn("Unable to publish metrics ..", e)
          None
        }
      }
    } else None
  }
}