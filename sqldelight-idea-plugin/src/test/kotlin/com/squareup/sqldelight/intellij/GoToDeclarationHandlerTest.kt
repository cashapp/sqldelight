package com.squareup.sqldelight.intellij

import com.alecstrong.sqlite.psi.core.psi.SqliteIdentifier
import com.google.common.truth.Truth.assertThat
import com.intellij.project.rootManager
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType

class GoToDeclarationHandlerTest : SqlDelightProjectTestCase() {
  private val goToDeclarationHandler = SqlDelightGotoDeclarationHandler()

  fun testMethodGoesToIdentifier() {
    val tempRoot = myModule.rootManager.contentRoots.single()
    myFixture.openFileInEditor(
        tempRoot.findFileByRelativePath("src/main/kotlin/com/example/SampleClass.java")!!
    )
    var offset = file.text.indexOf("someQuery")
    val sourceElement = file.findElementAt(offset)

    val elements = goToDeclarationHandler.getGotoDeclarationTargets(sourceElement, offset, editor)

    myFixture.openFileInEditor(
        tempRoot.findFileByRelativePath("src/main/sqldelight/com/example/Main.sq")!!
    )
    offset = file.text.indexOf("someQuery")
    assertThat(elements).asList().containsExactly(
        file.findElementAt(offset)!!.getStrictParentOfType<SqliteIdentifier>()
    )
  }
}
