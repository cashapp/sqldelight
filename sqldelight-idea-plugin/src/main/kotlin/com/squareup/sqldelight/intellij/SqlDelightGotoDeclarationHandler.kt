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
package com.squareup.sqldelight.intellij

import com.alecstrong.sqlite.psi.core.psi.SqliteIdentifier
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.project.rootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.squareup.sqldelight.core.SqlDelightFileIndex
import com.squareup.sqldelight.core.lang.SqlDelightFile
import com.squareup.sqldelight.core.lang.SqlDelightFileType
import com.squareup.sqldelight.core.lang.queriesName
import com.squareup.sqldelight.core.lang.util.findChildrenOfType
import com.squareup.sqldelight.core.psi.SqlDelightStmtIdentifier
import com.squareup.sqldelight.intellij.util.childOfType

class SqlDelightGotoDeclarationHandler : GotoDeclarationHandler {
  override fun getGotoDeclarationTargets(
    sourceElement: PsiElement?,
    offset: Int,
    editor: Editor
  ): Array<PsiElement> {
    if (sourceElement == null) return emptyArray()

    val module = sourceElement.module() ?: return emptyArray()

    val resolveElement = (sourceElement.parent as? PsiReference)?.resolve() as? PsiMethod ?: return emptyArray()
    val elementFile = resolveElement.containingFile.virtualFile ?: return emptyArray()

    // Only handle files under the generated sqlite directory.
    val fileIndex = SqlDelightFileIndex.getInstance(module)
    val outputDirectory = fileIndex.contentRoot.findFileByRelativePath(fileIndex.outputDirectory) ?: return emptyArray()
    if (!outputDirectory.isAncestorOf(elementFile)) return emptyArray()

    var result = emptyArray<PsiElement>()
    module.rootManager.fileIndex.iterateContent { vFile ->
      if (vFile.fileType != SqlDelightFileType
          || vFile.queriesName != elementFile.nameWithoutExtension) {
        return@iterateContent true
      }
      result = (PsiManager.getInstance(sourceElement.project).findFile(vFile) as SqlDelightFile)
          .sqlStmtList.findChildrenOfType<SqlDelightStmtIdentifier>()
          .mapNotNull { it.childOfType<SqliteIdentifier>() }
          .filter { it.text == sourceElement.text }
          .toTypedArray()
      return@iterateContent false
    }

    return result
  }

  override fun getActionText(context: DataContext) = null

  private fun VirtualFile.isAncestorOf(child: VirtualFile): Boolean {
    if (child in children) return true
    return isAncestorOf(child.parent ?: return false)
  }
}
