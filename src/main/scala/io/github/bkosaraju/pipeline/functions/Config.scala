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

trait Config {

  //Pipeline SQLs
  lazy val DB_INIT_SCRIPT           = "ddl/master_ddl.sql"

  lazy val JOB_VALIDATION_SQL       = "select/job_validator.sql"
  lazy val JOB_STATUS_SQL           = "select/job_status.sql"
  lazy val JOB_TASK_SQL             = "select/job_task.sql"
  lazy val JOB_ORDER_VALIDATION_SQL = "select/job_order_validation.sql"
  lazy val TASK_CONFIG_SQL          = "select/task_config.sql"
  lazy val PREV_TASK_STATS_SQL      = "select/prev_task_stats.sql"


  lazy val INIT_JOB_SQL             = "dml/init_job.sql"
  lazy val INIT_TASK_SQL            = "dml/init_proc.sql"
  lazy val UPDATE_TASK_SQL          = "dml/update_task_execution.sql"
  lazy val UPDATE_JOB_SQL           = "dml/update_job_execution.sql"

  //JobConfigurations
  lazy val START                    = "START"
  lazy val END                      = "END"
  lazy val FAIL                     = "FAIL"
  lazy val RERUN                    = "RERUN"
  lazy val OVERRIDE                 = "OVERRIDE"
  lazy val FIRST_RUN                 = "FIRST_RUN"

}
