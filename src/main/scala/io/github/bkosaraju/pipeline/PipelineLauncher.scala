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

package io.github.bkosaraju.pipeline

import io.github.bkosaraju.pipeline.functions.{ArgReader, RunJob}
import picocli.CommandLine

import scala.collection.JavaConverters._


object PipelineLauncher extends AppFunctions {

  def main(args: Array[String]): Unit = {
    CommandLine.run(new ArgReader(),System.err,args: _*)
      val cmd = CommandLine.populateCommand(new ArgReader, args: _*)
      val props = cmd.properties.loadParms("app.properties")
      props.setProperty("jobId", cmd.jobId)
      props.setProperty("jobOrderTimestamp",cmd.orderDateTime)
      props.setProperty("rerunFlag", cmd.resetPrevRun.toString)
      props.setProperty("endRunFlag", cmd.endprevRun.toString)
      RunJob(props.asInstanceOf[java.util.Map[String,String]].asScala)
    }
}