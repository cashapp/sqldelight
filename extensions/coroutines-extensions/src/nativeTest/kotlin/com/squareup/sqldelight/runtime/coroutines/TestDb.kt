/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squareup.sqldelight.runtime.coroutines

import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext

private fun defaultSchema(): SqlDriver.Schema<SqlPreparedStatement, SqlCursor> {
  return object : SqlDriver.Schema<SqlPreparedStatement, SqlCursor> {
    override val version: Int = 1
    override fun create(driver: SqlDriver<SqlPreparedStatement, SqlCursor>) {}
    override fun migrate(
      driver: SqlDriver<SqlPreparedStatement, SqlCursor>,
      oldVersion: Int,
      newVersion: Int
    ) {}
  }
}

actual suspend fun testDriver(): SqlDriver<SqlPreparedStatement, SqlCursor> {
  val name = "testdb"
  DatabaseFileContext.deleteDatabase(name)
  return NativeSqliteDriver(defaultSchema(), name)
}
