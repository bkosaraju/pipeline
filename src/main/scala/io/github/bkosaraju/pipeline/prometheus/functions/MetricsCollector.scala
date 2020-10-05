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

import io.github.bkosaraju.pipeline.prometheus.model.SummaryMetric
import io.github.bkosaraju.utils.common.Session
import io.github.bkosaraju.pipeline.prometheus.model.{CounterMetric, GaugeMetric, HistogramMetric, SummaryMetric}
import io.prometheus.client.{CollectorRegistry, Counter, Gauge, Histogram, Summary}

import collection.JavaConverters._
import scala.beans.BeanProperty


class MetricsCollector extends Session{


  def initGaugeMetric(
                       metricName: String,
                       help: String,
                       //registry: CollectorRegistry,
                       labels : Map[String,String]
                     ): GaugeMetric = {
    try {
      val labelNames = labels.keySet.toSeq
      val lableValues = labels.values.toSeq
    val gauge = Gauge
      .build(metricName, help)
      .labelNames(labelNames: _*)
      .register()
     // .register(registry)
    val gm = new GaugeMetric
    gm.setGauge(gauge)
    gm.setGaugeStartTimer(gauge.labels(lableValues: _* ).startTimer())
  //  gm.setGaugeRegistry(registry)
    gm
  } catch {
      case e: Exception => {
        logger.warn("Unable to Initialize metrics")
        throw e
      }
    }
  }

  def initCounterMetric(
                         metricName: String,
                         help: String,
                         registry: CollectorRegistry,
                         labelNames: String*
                       ) : CounterMetric = {
    try {
    val counter = Counter
      .build(metricName, help)
      .labelNames(labelNames: _*)
      .register(registry)
    counter.inc()
    val cntr = new CounterMetric
    cntr.setCounter(counter)
    cntr
    } catch {
      case e: Exception => {
        logger.warn("Unable to Initialize metrics")
        throw e
      }
    }
  }

  def initHistogramMetric(
                         metricName: String,
                         help: String,
                         registry: CollectorRegistry,
                         labelNames: String*
                       ) : HistogramMetric = {
    try {
    val histogram = Histogram
      .build(metricName, help)
      .labelNames(labelNames: _*)
      .register(registry)

    val hstgrm = new HistogramMetric
    hstgrm.setHistogram(histogram)
    hstgrm
    } catch {
      case e: Exception => {
        logger.warn("Unable to Initialize metrics")
        throw e
      }
    }
  }

  def initHistogramSummary(
                           metricName: String,
                           help: String,
                           registry: CollectorRegistry,
                           labelNames: String*
                         ) : SummaryMetric = {
    try {
    val summary = Summary
      .build(metricName, help)
      .labelNames(labelNames: _*)
      .register(registry)

    val smry = new SummaryMetric
    smry.setHistogram(summary)
    smry
  } catch {
    case e: Exception => {
      logger.warn("Unable to Initialize metrics")
      throw e
    }
  }
}

}
