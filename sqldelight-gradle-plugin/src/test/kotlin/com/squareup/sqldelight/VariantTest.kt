package com.squareup.sqldelight

import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.core.SqlDelightPropertiesFile
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File

class VariantTest {
  @Test
  fun `A table queried from the main source set must be consistent for all variants`() {
    val fixtureRoot = File("src/test/fixtures/fulfilled-table-variant")
    val androidHome = androidHome()
    File(fixtureRoot, "local.properties").writeText("sdk.dir=$androidHome\n")

    val runner = GradleRunner.create()
        .withProjectDir(fixtureRoot)
        .withPluginClasspath()

    var result = runner
        .withArguments("clean", "generateInternalSqlDelightInterface",
            "--stacktrace", "-Dsqldelight.skip.runtime=true")
        .buildAndFail()
    assertThat(result.output).contains("""
      MainTable.sq line 7:12 - No column found with name some_column1
      7    SELECT _id, some_column1
                       ^^^^^^^^^^^^
      8    FROM some_table

       FAILED
      """.trimIndent())

    runner.withArguments("clean", "generateReleaseSqlDelightInterface",
            "--stacktrace", "-Dsqldelight.skip.runtime=true")
        .build()
  }

  @Test
  fun `The gradle plugin resolves with multiple source sets`() {
    val fixtureRoot = File("src/test/fixtures/variants")
    val androidHome = androidHome()
    File(fixtureRoot, "local.properties").writeText("sdk.dir=$androidHome\n")

    val runner = GradleRunner.create()
        .withProjectDir(fixtureRoot)
        .withPluginClasspath()

    val result = runner
        .withArguments("clean", "assemble", "--stacktrace", "-Dsqldelight.skip.runtime=true",
            "--continue")
        .buildAndFail()
    assertThat(result.output).contains("""
      src/minApi21DemoDebug/sqldelight/com/sample/demo/debug/DemoDebug.sq line 7:5 - No table found with name full_table
      6    SELECT *
      7    FROM full_table
                ^^^^^^^^^^
      """.trimIndent())
  }

  @Test
  fun `The gradle plugin generates a properties file with the application id and all source sets`() {
    val fixtureRoot = File("src/test/fixtures/working-variants")
    val androidHome = androidHome()
    File(fixtureRoot, "local.properties").writeText("sdk.dir=$androidHome\n")

    val runner = GradleRunner.create()
        .withProjectDir(fixtureRoot)
        .withPluginClasspath()

    runner
        .withArguments("clean", "assemble", "--stacktrace", "-Dsqldelight.skip.runtime=true",
            "--continue")
        .build()

    // verify
    val propertiesFile = File(fixtureRoot, SqlDelightPropertiesFile.NAME)
    assertThat(propertiesFile.exists()).isTrue()

    val properties = SqlDelightPropertiesFile.fromFile(propertiesFile)
    assertThat(properties.packageName).isEqualTo("com.sample")
    assertThat(properties.sourceSets).hasSize(2)

    with(properties.sourceSets[0]) {
      assertThat(this).hasSize(2)
      assertThat(this[0]).contains("main")
      assertThat(this[1]).contains("debug")
    }

    with(properties.sourceSets[1]) {
      assertThat(this).hasSize(2)
      assertThat(this[0]).contains("main")
      assertThat(this[1]).contains("release")
    }
  }
}
