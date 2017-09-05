package com.squareup.sqldelight.core.tables

import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.core.TestEnvironment
import com.squareup.sqldelight.core.compiler.SqlDelightCompiler
import com.squareup.sqldelight.core.lang.SqlDelightFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.util.LinkedHashMap

@RunWith(Parameterized::class)
class InterfaceGeneration(val fixtureRoot: File, val name: String) {
  @Test fun execute() {
    val parser = TestEnvironment()
    val environment = parser.build(fixtureRoot.path)
    val output = LinkedHashMap<String, StringBuilder>()

    environment.forSourceFiles { psiFile ->
      SqlDelightCompiler.writeInterfaces(psiFile as SqlDelightFile) { fileName ->
        val builder = StringBuilder()
        output.put(fileName, builder)
        return@writeInterfaces builder
      }
    }

    for ((fileName, actualOutput) in output) {
      val expectedFile = File(fixtureRoot, fileName)
      assertThat(expectedFile.exists()).named("No file with name $expectedFile").isTrue()

      assertThat(expectedFile.readText()).named(fileName).isEqualTo(actualOutput.toString())
    }
  }

  companion object {
    @Suppress("unused") // Used by Parameterized JUnit runner reflectively.
    @Parameters(name = "{1}")
    @JvmStatic fun parameters(): List<Array<Any>> =
        File("src/test/table-interface-fixtures").listFiles()
            .filter { it.isDirectory }
            .map { arrayOf(it, it.name) }
  }
}