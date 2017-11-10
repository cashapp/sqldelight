/*
 * Copyright (C) 2017 Square, Inc.
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
package com.squareup.sqldelight.core.queries

import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.core.compiler.SelectQueryGenerator
import com.squareup.sqldelight.core.compiler.namedQueries
import com.squareup.sqldelight.core.util.FixtureCompiler
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class QueriesPropertyTest {
  @get:Rule val tempFolder = TemporaryFolder()

  @Test fun `query property generates properly`() {
    // This barely tests anything but its easier to verify the codegen works like this.
    val file = FixtureCompiler.parseSql("""
      |selectForId:
      |SELECT *
      |FROM data
      |WHERE _id = ?;
      """.trimMargin(), tempFolder)

    val generator = SelectQueryGenerator(file.sqliteStatements().namedQueries().first())

    assertThat(generator.queryCollectionProperty().toString())
        .isEqualTo("""
          |private val selectForIdQueries: kotlin.collections.MutableList<com.squareup.sqldelight.Query<*>> = mutableListOf<>()
          |
        """.trimMargin())
  }
}
