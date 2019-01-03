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
package com.squareup.sqldelight.db

import com.squareup.sqldelight.Transacter

interface SqlDatabase : Closeable {
  /**
   * Prepare [sql] into a bindable object to be executed later.
   *
   * [identifier] can be used to implement any driver-side caching of prepared statements.
   *   If [identifier] is null, a fresh statement is required.
   * [parameters] is the number of bindable parameters [sql] contains.
   */
  fun executeQuery(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)? = null
  ): SqlCursor

  /**
   * Executes the SQL statement in this [SqlPreparedStatement], which must be an
   * SQL Data Manipulation Language (DML) statement, such as `INSERT`, `UPDATE` or
   * `DELETE`; or an SQL statement that returns nothing, such as a DDL statement.
   */
  fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)? = null
  )

  /**
   * Start a new [Transacter.Transaction] for this connection.
   */
  fun newTransaction(): Transacter.Transaction

  /**
   * The currently open [Transacter.Transaction] for this connection.
   */
  fun currentTransaction(): Transacter.Transaction?

  interface Schema {
    val version: Int
    fun create(db: SqlDatabase)
    fun migrate(db: SqlDatabase, oldVersion: Int, newVersion: Int)
  }
}
