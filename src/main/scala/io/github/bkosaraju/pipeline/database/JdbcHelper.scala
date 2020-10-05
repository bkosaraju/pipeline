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

import io.github.bkosaraju.utils.common.LoadResourceAsString

import org.jdbi.v3.core.{Jdbi,Handle}

trait JdbcHelper extends io.github.bkosaraju.utils.database.JdbcHelper {

  implicit class GetResourcePath (val jdbi: Jdbi) extends LoadResourceAsString {
    def getresource( resourceLocation : String ) : String = {
         loadResource("db/" + getDBProductName(jdbi) +"/" + resourceLocation)
    }
  }

  def getDBProductName(jdbi : Jdbi): String = {
    var dbType : String = ""
    using(jdbi.open()){ handle => dbType = handle.getConnection.getMetaData.getDatabaseProductName }
    dbType
  }


}
