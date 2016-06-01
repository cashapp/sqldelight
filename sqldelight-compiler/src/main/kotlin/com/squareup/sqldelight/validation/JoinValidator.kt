/*
 * Copyright (C) 2016 Square, Inc.
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
package com.squareup.sqldelight.validation

import com.squareup.sqldelight.SqliteParser
import com.squareup.sqldelight.resolution.Resolver
import com.squareup.sqldelight.resolution.ResolutionError
import com.squareup.sqldelight.types.Value

internal class JoinValidator(
    private val resolver: Resolver,
    private val values: List<Value>,
    private val scopedValues: List<Value>
) {
  fun validate(joinConstraint: SqliteParser.Join_constraintContext): List<ResolutionError> {
    val response = arrayListOf<ResolutionError>()

    if (joinConstraint.K_ON() != null) {
      // : ( K_ON expr
      response.addAll(ExpressionValidator(resolver, values + scopedValues)
          .validate(joinConstraint.expr()))
    }

    if (joinConstraint.K_USING() != null) {
      // | K_USING '(' column_name ( ',' column_name )* ')' )?
      joinConstraint.column_name().forEach { column_name ->
        // This column name must be in the scoped values (outside this join) and values (inside join)
        if (!values.any { it.columnName == column_name.text }) {
          response.add(ResolutionError.ColumnNameNotFound(
              column_name,
              "Joined table or subquery does not contain a column with the name ${column_name.text}",
              values.filter { value -> scopedValues.any { it.columnName == value.columnName } }
          ))
        }
        if (!scopedValues.any { it.columnName == column_name.text }) {
          response.add(ResolutionError.ColumnNameNotFound(
              column_name,
              "Table joined against does not contain a column with the name ${column_name.text}",
              values.filter { value -> scopedValues.any { it.columnName == value.columnName } }
          ))
        }
      }
    }
    return response
  }
}
