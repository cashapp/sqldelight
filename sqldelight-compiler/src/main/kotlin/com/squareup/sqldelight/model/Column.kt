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
package com.squareup.sqldelight.model

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ClassName.bestGuess
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.sqldelight.SqliteCompiler
import com.squareup.sqldelight.SqlitePluginException
import com.squareup.sqldelight.model.Column.Type.BLOB
import com.squareup.sqldelight.model.Column.Type.BOOLEAN
import com.squareup.sqldelight.model.Column.Type.DOUBLE
import com.squareup.sqldelight.model.Column.Type.ENUM
import com.squareup.sqldelight.model.Column.Type.FLOAT
import com.squareup.sqldelight.model.Column.Type.INT
import com.squareup.sqldelight.model.Column.Type.LONG
import com.squareup.sqldelight.model.Column.Type.SHORT
import com.squareup.sqldelight.model.Column.Type.STRING
import com.squareup.sqldelight.model.ColumnConstraint.NotNullConstraint
import java.util.ArrayList
import java.util.Locale.US

class Column<T>(internal val name: String, val type: Type, fullyQualifiedClass: String? = null,
    originatingElement: T) : SqlElement<T>(originatingElement) {
  fun adapterType() = ParameterizedTypeName.get(SqliteCompiler.COLUMN_ADAPTER_TYPE, javaType)
  fun adapterField() = Column.adapterField(name)

  fun marshaledValue(name: String) =
      when (type) {
        INT, LONG, SHORT, DOUBLE, FLOAT,
        STRING, BLOB -> name
        BOOLEAN -> "$name ? 1 : 0"
        ENUM -> "$name.name()"
        else -> throw IllegalStateException("Unexpected type")
      }

  enum class Type constructor(internal val defaultType: TypeName?, val replacement: String) {
    INT(TypeName.INT, "INTEGER"),
    LONG(TypeName.LONG, "INTEGER"),
    SHORT(TypeName.SHORT, "INTEGER"),
    DOUBLE(TypeName.DOUBLE, "REAL"),
    FLOAT(TypeName.FLOAT, "REAL"),
    BOOLEAN(TypeName.BOOLEAN, "INTEGER"),
    STRING(ClassName.get(String::class.java), "TEXT"),
    BLOB(ArrayTypeName.of(TypeName.BYTE), "BLOB"),
    ENUM(null, "TEXT"),
    CLASS(null, "BLOB")
  }

  private val classType: TypeName?

  internal val javaType: TypeName
    get() = when {
      type.defaultType == null && classType != null -> classType
      type.defaultType == null -> throw SqlitePluginException(originatingElement as Any,
          "Couldn't make a guess for type of column " + name)
      notNullConstraint != null -> type.defaultType
      else -> type.defaultType.box()
    }

  val constraints: MutableList<ColumnConstraint<T>> = ArrayList()
  val isHandledType = type != Type.CLASS
  val constantName = constantName(name)
  val methodName = methodName(name)
  val notNullConstraint: NotNullConstraint<T>?
    get() = constraints.filterIsInstance<NotNullConstraint<T>>().firstOrNull()
  val isNullable: Boolean
    get() = notNullConstraint == null

  init {
    var className = fullyQualifiedClass
    try {
      classType = when {
        className == null -> null
        className.startsWith("\'") -> bestGuess(className.substring(1, className.length - 1))
        else -> bestGuess(className)
      }
    } catch (ignored: IllegalArgumentException) {
      classType = null
    }
  }

  companion object {
    fun constantName(name: String) = name.toUpperCase(US)
    fun methodName(name: String) = name
    fun adapterField(name: String) = name + "Adapter"
  }
}
