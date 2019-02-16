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
package com.squareup.sqldelight.core.compiler

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.DATA
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.sqldelight.core.compiler.model.NamedQuery
import com.squareup.sqldelight.core.lang.IMPLEMENTATION_NAME
import com.squareup.sqldelight.core.lang.util.sqFile

class QueryInterfaceGenerator(val query: NamedQuery) {
  private fun kotlinImplementationSpec(): TypeSpec {
    val typeSpec = TypeSpec.classBuilder(IMPLEMENTATION_NAME)
        .addModifiers(DATA)
        .addSuperinterface(ClassName(query.select.sqFile().packageName, query.name.capitalize()))

    val propertyPrints = CodeBlock.builder()
    propertyPrints.beginControlFlow("buildString")
    propertyPrints.addStatement("appendln(%S)", "${query.name.capitalize()}.$IMPLEMENTATION_NAME [")

    val constructor = FunSpec.constructorBuilder()
    val contentToString = MemberName("kotlin.collections", "contentToString")

    query.resultColumns.forEach {
      typeSpec.addProperty(PropertySpec.builder(it.name, it.javaType, OVERRIDE)
          .initializer(it.name)
          .build())
      constructor.addParameter(it.name, it.javaType, OVERRIDE)

      propertyPrints.addStatement("appendln(%P)", buildCodeBlock {
        add("  ${it.name}: ")
        if (it.javaType == ByteArray::class.asTypeName()) {
          add("\${${it.name}.%M()}", contentToString)
        } else {
          add("\$${it.name}")
        }
      })
    }
    propertyPrints.addStatement("append(%S)", "]")
    propertyPrints.endControlFlow()

    typeSpec.addFunction(FunSpec.builder("toString")
        .returns(String::class.asClassName())
        .addModifiers(OVERRIDE)
        .addCode("return %L", propertyPrints.build())
        .build()
    )

    return typeSpec.primaryConstructor(constructor.build()).build()
  }

  fun kotlinInterfaceSpec(): TypeSpec {
    val typeSpec = TypeSpec.interfaceBuilder(query.name.capitalize())

    query.resultColumns.forEach {
      typeSpec.addProperty(it.name, it.javaType, PUBLIC)
    }

    return typeSpec
        .addType(kotlinImplementationSpec())
        .build()
  }
}
