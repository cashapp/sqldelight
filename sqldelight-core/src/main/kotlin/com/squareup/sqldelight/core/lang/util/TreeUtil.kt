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
package com.squareup.sqldelight.core.lang.util

import com.alecstrong.sqlite.psi.core.psi.AliasElement
import com.alecstrong.sqlite.psi.core.psi.SqliteColumnName
import com.alecstrong.sqlite.psi.core.psi.SqliteExpr
import com.alecstrong.sqlite.psi.core.psi.SqliteTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.squareup.sqldelight.core.lang.IntermediateType
import com.squareup.sqldelight.core.lang.SqlDelightFile
import com.squareup.sqldelight.core.lang.psi.ColumnDefMixin
import com.squareup.sqldelight.core.lang.psi.InsertStmtMixin

internal inline fun <reified R: PsiElement> PsiElement.parentOfType(): R {
  return PsiTreeUtil.getParentOfType(this, R::class.java)!!
}

internal fun PsiElement.type(): IntermediateType = when (this) {
  is AliasElement -> source().type().copy(name = name)
  is SqliteColumnName -> {
    val parentRule = parent!!
    when (parentRule) {
      is ColumnDefMixin -> parentRule.type()
      else -> reference!!.resolve()!!.type()
    }
  }
  is SqliteExpr -> type()
  else -> throw IllegalStateException("Cannot get function type for psi type ${this.javaClass}")
}

internal fun PsiElement.sqFile(): SqlDelightFile = containingFile as SqlDelightFile

inline fun <reified T: PsiElement> PsiElement.findChildrenOfType(): Collection<T> {
  return PsiTreeUtil.findChildrenOfType(this, T::class.java)
}

fun PsiElement.childOfType(type: IElementType): PsiElement? {
  return node.findChildByType(type)?.psi
}

fun PsiElement.childOfType(types: TokenSet): PsiElement? {
  return node.findChildByType(types)?.psi
}

inline fun <reified T: PsiElement> PsiElement.nextSiblingOfType(): T {
  return PsiTreeUtil.getNextSiblingOfType(this, T::class.java)!!
}

private fun PsiElement.rangesToReplace(): List<Pair<IntRange, String>> {
  return if (this is ColumnDefMixin && javaTypeName != null) {
    listOf(Pair(
        first = (typeName.node.startOffset + typeName.node.textLength) until
            (javaTypeName!!.node.startOffset + javaTypeName!!.node.textLength),
        second = ""
    ))
  } else if (this is InsertStmtMixin && acceptsTableInterface()) {
    listOf(Pair(
        first = childOfType(SqliteTypes.BIND_EXPR)!!.range,
        second = columns.joinToString(separator = ", ", prefix = "(", postfix = ")") { "?" }
    ))
  } else {
    children.flatMap { it.rangesToReplace() }
  }
}

private operator fun IntRange.minus(amount: Int): IntRange {
  return IntRange(start - amount, endInclusive - amount)
}

private val IntRange.length: Int
    get() = endInclusive - start + 1

fun PsiElement.rawSqlText(
  replacements: List<Pair<IntRange, String>> = emptyList()
): String {
  return (replacements + rangesToReplace())
      .sortedBy { it.first.start }
      .map { (range, replacement) -> (range - node.startOffset) to replacement }
      .fold(0 to text, { (totalRemoved, sqlText), (range, replacement) ->
        (totalRemoved + (range.length - replacement.length)) to sqlText.replaceRange(range - totalRemoved, replacement)
      }).second
}

internal val PsiElement.range: IntRange
  get() = node.startOffset until (node.startOffset + node.textLength)
