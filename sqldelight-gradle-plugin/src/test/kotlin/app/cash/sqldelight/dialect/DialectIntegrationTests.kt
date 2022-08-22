package app.cash.sqldelight.dialect

import app.cash.sqldelight.assertions.FileSubject
import app.cash.sqldelight.withCommonConfiguration
import com.google.common.truth.Truth
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File

class DialectIntegrationTests {

  @Test fun integrationTestsMySqlSchemaOutput() {
    val integrationRoot = File("src/test/schema-output")

    val runner = GradleRunner.create()
      .withCommonConfiguration(integrationRoot)
      .withArguments("clean", "generateMainMyDatabaseMigrations", "--stacktrace")

    val result = runner.build()
    Truth.assertThat(result.output).contains("BUILD SUCCESSFUL")

    FileSubject.assertThat(File(integrationRoot, "build"))
      .contentsAreEqualTo(File(integrationRoot, "expected-build"))
  }

  @Test fun integrationTestsHsql() {
    val runner = GradleRunner.create()
      .withCommonConfiguration(File("src/test/integration-hsql"))
      .withArguments("clean", "check", "--stacktrace")

    val result = runner.build()
    Truth.assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  @Test fun integrationTestWithModule() {
    val runner = GradleRunner.create()
      .withCommonConfiguration(File("src/test/integration-module"))
      .withArguments("clean", "build", "--stacktrace")

    val result = runner.build()
    Truth.assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  @Test fun integrationTestsMultiDialect() {
    val runner = GradleRunner.create()
      .withCommonConfiguration(File("src/test/integration-multi-dialect-modules"))
      .withArguments("clean", "build", "--stacktrace")

    val result = runner.build()
    Truth.assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  @Test fun customFunctionDialect() {
    val runner = GradleRunner.create()
      .withCommonConfiguration(File("src/test/custom-dialect"))
      .withArguments("clean", "compileTestKotlin", "--stacktrace")

    val result = runner.build()
    Truth.assertThat(result.output).contains("BUILD SUCCESSFUL")
  }

  @Test fun embeddedSql() {
    val runner = GradleRunner.create()
      .withCommonConfiguration(File("src/test/embedded-sql"))
      .withArguments("clean", "compileTestKotlin", "--stacktrace")

    val result = runner.build()
    Truth.assertThat(result.output).contains("BUILD SUCCESSFUL")
  }
}
