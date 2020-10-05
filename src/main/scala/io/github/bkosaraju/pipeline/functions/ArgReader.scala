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

import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters

import scala.Boolean


@Command(
  name = "pipeline",
  version = Array("Pipeline to run user applications in sequence"),
  mixinStandardHelpOptions = true,
  description = Array("@|bold Pipeline to run user applications in sequence|@ example:")
)
class ArgReader extends Runnable {

  @Option(
    names = Array("-p", "--properties"),
    description = Array("properties for input application")
    ,required = true)
  var properties : String = _

  @Option(
    names = Array("-j", "--jobid"),
    description = Array("jobId to be executed")
    ,required = true)
  var jobId: String = _

  @Option(
    names = Array("-d", "--ordertimestamp"),
    description = Array("Job Order date must be specified in YYYY-mm-dd or YYYY-mm-DDTHH:MM:SS[Z]")
    ,required = true)
  var orderDateTime: String = ""

  @Option(
    names = Array("-r", "--restartjobstatusandrun and rerun"),
    description = Array("Set the previous job execution status to RERUN and run the job")
    ,required = false)
  var resetPrevRun: Boolean = _

  @Option(
    names = Array("-e", "--endjobstatusandrun and run"),
    description = Array("Set the previous job execution status to END and run the job")
    ,required = false)
  var endprevRun: Boolean = _
  //Runner Implementation
  def run() : Unit = {}
}
