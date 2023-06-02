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
package app.cash.sqldelight.gradle

import app.cash.sqldelight.VERSION
import app.cash.sqldelight.core.MINIMUM_SUPPORTED_VERSION
import app.cash.sqldelight.core.SqlDelightPropertiesFile
import app.cash.sqldelight.gradle.android.packageName
import app.cash.sqldelight.gradle.android.sqliteVersion
import app.cash.sqldelight.gradle.kotlin.linkSqlite
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

internal const val MIN_GRADLE_VERSION = "7.0"

abstract class SqlDelightPlugin : Plugin<Project> {
  private val android = AtomicBoolean(false)
  private val kotlin = AtomicBoolean(false)

  @get:Inject
  abstract val registry: ToolingModelBuilderRegistry

  private lateinit var extension: SqlDelightExtension

  override fun apply(project: Project) {
    require(GradleVersion.current() >= GradleVersion.version(MIN_GRADLE_VERSION)) {
      "SQLDelight requires Gradle version $MIN_GRADLE_VERSION or greater."
    }

    extension = project.extensions.create("sqldelight", SqlDelightExtension::class.java)

    project.plugins.withId("com.android.base") {
      android.set(true)
      project.afterEvaluate {
        project.setupSqlDelightTasks(afterAndroid = true)
      }
    }

    project.plugins.withType(KotlinBasePlugin::class.java) {
      kotlin.set(true)
    }

    project.tasks.register("generateSqlDelightInterface") {
      it.group = GROUP
      it.description = "Aggregation task which runs every interface generation task for every given source"
    }

    project.tasks.register("verifySqlDelightMigration") {
      it.group = GROUP
      it.description = "Aggregation task which runs every migration task for every given source"
    }

    project.afterEvaluate {
      project.setupSqlDelightTasks(afterAndroid = false)
    }
  }

  private fun Project.setupSqlDelightTasks(afterAndroid: Boolean) {
    if (android.get() && !afterAndroid) return

    check(kotlin.get()) {
      "SQL Delight Gradle plugin applied in " +
        "project '${project.path}' but no supported Kotlin plugin was found"
    }

    val isMultiplatform = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
    val isJsOnly = if (isMultiplatform) false else project.plugins.hasPlugin("org.jetbrains.kotlin.js")

    val needsAsyncRuntime = extension.databases.any { it.generateAsync.get() }
    val runtimeDependencies = mutableListOf<Dependency>().apply {
      add(project.dependencies.create("app.cash.sqldelight:runtime:$VERSION"))
      if (needsAsyncRuntime) add(project.dependencies.create("app.cash.sqldelight:async-extensions:$VERSION"))
    }

    // Add the runtime dependency.
    when {
      isMultiplatform -> {
        val sourceSets =
          project.extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
        val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)
        project.configurations.getByName(sourceSet.apiConfigurationName)
          .dependencies.addAll(runtimeDependencies)
      }
      isJsOnly -> {
        val sourceSets =
          project.extensions.getByType(KotlinJsProjectExtension::class.java).sourceSets
        val sourceSet = (sourceSets.getByName("main") as DefaultKotlinSourceSet)
        project.configurations.getByName(sourceSet.apiConfigurationName)
          .dependencies.addAll(runtimeDependencies)
      }
      else -> {
        project.configurations.getByName("api").dependencies.addAll(runtimeDependencies)
      }
    }

    if (extension.linkSqlite.getOrElse(true)) {
      project.linkSqlite()
    }

    extension.run {
      if (databases.isEmpty() && android.get() && !isMultiplatform) {
        // Default to a database for android named "Database" to keep things simple.
        databases.add(
          objects.newInstance(SqlDelightDatabase::class.java, project, "Database").apply {
            packageName.set(project.packageName())
            project.sqliteVersion()?.let(::dialect)
          },
        )
      } else if (databases.isEmpty()) {
        logger.warn("SQLDelight Gradle plugin was applied but there are no databases set up.")
      }

      databases.forEach { database ->
        if (database.packageName.getOrNull() == null && android.get() && !isMultiplatform) {
          database.packageName.set(project.packageName())
        }
        if (!database.addedDialect && android.get() && !isMultiplatform) {
          project.sqliteVersion()?.let(database::dialect)
        }
        if (!database.addedDialect) {
          database.dialect("app.cash.sqldelight:sqlite-3-18-dialect:$VERSION")
        }
        database.registerTasks()
      }

      registry.register(PropertiesModelBuilder(databases))
    }
  }

  class PropertiesModelBuilder(
    private val databases: Iterable<SqlDelightDatabase>,
  ) : ToolingModelBuilder {
    override fun canBuild(modelName: String): Boolean {
      return modelName == SqlDelightPropertiesFile::class.java.name
    }

    override fun buildAll(modelName: String, project: Project): Any {
      return SqlDelightPropertiesFileImpl(
        databases = databases.map { it.getProperties() },
        currentVersion = VERSION,
        minimumSupportedVersion = MINIMUM_SUPPORTED_VERSION,
        dialectJars = databases.first().configuration.files,
      )
    }
  }

  internal companion object {
    const val GROUP = "sqldelight"
  }
}
