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
package com.squareup.sqldelight.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.errors.SyncIssueHandlerImpl
import com.android.build.gradle.options.SyncOptions.EvaluationMode.STANDARD
import com.android.builder.core.DefaultManifestParser
import com.squareup.sqldelight.VERSION
import com.squareup.sqldelight.core.SqlDelightPropertiesFile
import com.squareup.sqldelight.core.lang.MigrationFileType
import com.squareup.sqldelight.core.lang.SqlDelightFileType
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

open class SqlDelightPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("sqldelight", SqlDelightExtension::class.java)

    var kotlin = false
    var android = false

    project.plugins.all {
      when (it) {
        is KotlinBasePluginWrapper -> {
          kotlin = true
        }
        is BasePlugin<*> -> {
          android = true
        }
      }
    }

    project.afterEvaluate {
      if (!kotlin) {
        throw IllegalStateException("SQL Delight Gradle plugin applied in "
            + "project '${project.path}' but no supported Kotlin plugin was found")
      }
      if (android) {
        val variants: DomainObjectSet<out BaseVariant> = when {
          project.plugins.hasPlugin("com.android.application") -> {
            project.extensions.getByType(AppExtension::class.java).applicationVariants
          }
          project.plugins.hasPlugin("com.android.library") -> {
            project.extensions.getByType(LibraryExtension::class.java).libraryVariants
          }
          else -> {
            throw IllegalStateException("Unknown Android plugin in project '${project.path}'")
          }
        }
        configureAndroid(project, extension, variants)
      } else {
        configureKotlin(project, extension)
      }
    }
  }

  private fun configureKotlin(project: Project, extension: SqlDelightExtension) {
    val outputDirectory = File(project.buildDir, "sqldelight")

    val sourceSets = project.property("sourceSets") as SourceSetContainer
    sourceSets.getByName("main").kotlin!!.srcDirs(outputDirectory.toRelativeString(project.projectDir))

    project.afterEvaluate {
      val packageName = requireNotNull(extension.packageName) { "property packageName must be provided" }
      val sourceSet = extension.sourceSet ?: project.files("src/main/sqldelight")

      val ideaDir = File(project.rootDir, ".idea")
      if (ideaDir.exists()) {
        val propsDir =
          File(ideaDir, "sqldelight/${project.projectDir.toRelativeString(project.rootDir)}")
        propsDir.mkdirs()

        val properties = SqlDelightPropertiesFile(
            packageName = packageName,
            sourceSets = listOf(sourceSet.map { it.toRelativeString(project.projectDir) }),
            outputDirectory = outputDirectory.toRelativeString(project.projectDir)
        )
        properties.toFile(File(propsDir, SqlDelightPropertiesFile.NAME))
      }

      val task = project.tasks.create("generateSqlDelightInterface", SqlDelightTask::class.java) {
        it.packageName = packageName
        it.sourceFolders = sourceSet.files
        it.outputDirectory = outputDirectory
        it.source(sourceSet)
        it.include("**${File.separatorChar}*.${SqlDelightFileType.defaultExtension}")
        it.include("**${File.separatorChar}*.${MigrationFileType.defaultExtension}")
      }
      task.group = "sqldelight"
      task.description = "Generate Kotlin interfaces for .sq files"

      project.tasks.findByName("compileKotlin")?.dependsOn(task)
      project.tasks.findByName("compileKotlinCommon")?.dependsOn(task)

      addMigrationTasks(project, sourceSet.files, extension.schemaOutputDirectory)
    }
  }

  private fun configureAndroid(project: Project, extension: SqlDelightExtension,
      variants: DomainObjectSet<out BaseVariant>) {
    val compileDeps = project.configurations.getByName("api").dependencies
    if (System.getProperty("sqldelight.skip.runtime") != "true") {
      compileDeps.add(project.dependencies.create("com.squareup.sqldelight:runtime-jdk:$VERSION"))
    }

    var packageName: String? = null
    val sourceSets = mutableListOf<List<String>>()
    val buildDirectory = listOf("generated", "source", "sqldelight").fold(project.buildDir, ::File)

    variants.all {
      val taskName = "generate${it.name.capitalize()}SqlDelightInterface"
      val task = project.tasks.create(taskName, SqlDelightTask::class.java)
      task.group = "sqldelight"
      task.outputDirectory = buildDirectory
      task.description = "Generate Android interfaces for working with ${it.name} database tables"
      task.source(it.sourceSets.map { "src/${it.name}/${SqlDelightFileType.FOLDER_NAME}" })
      task.include("**${File.separatorChar}*.${SqlDelightFileType.defaultExtension}")
      task.include("**${File.separatorChar}*.${MigrationFileType.defaultExtension}")
      task.packageName = it.packageName(project)
      task.sourceFolders = it.sourceSets.map { File("${project.projectDir}/src/${it.name}/${SqlDelightFileType.FOLDER_NAME}") }

      sourceSets.add(task.sourceFolders.map { it.toRelativeString(project.projectDir) })
      packageName = task.packageName

      it.registerJavaGeneratingTask(task, task.outputDirectory)
    }

    project.afterEvaluate {
      val ideaDir = File(project.rootDir, ".idea")
      if (ideaDir.exists()) {
        val propsDir =
            File(ideaDir, "sqldelight/${project.projectDir.toRelativeString(project.rootDir)}")
        propsDir.mkdirs()

        val properties = SqlDelightPropertiesFile(
            packageName = packageName!!,
            sourceSets = sourceSets,
            outputDirectory = buildDirectory.toRelativeString(project.projectDir)
        )
        properties.toFile(File(propsDir, SqlDelightPropertiesFile.NAME))
      }

      addMigrationTasks(
          project = project,
          sourceSet = sourceSets.flatten().distinct().map { File(project.projectDir, it) },
          schemaOutputDirectory = extension.schemaOutputDirectory
              ?: File(project.projectDir, "src/main/sqldelight")
      )
    }
  }

  /**
   * Theres no external api to get the package name. There is to get the application id, but thats
   * the post build package for the play store, and not the package name that should be used during
   * compilation. Think R.java, we want to be using the same namespace as it.
   *
   * There IS an internal api for doing this.
   * [BaseVariantImpl.getVariantData().getVariantConfiguration().getPackageFromManifest()],
   * and so this code just emulates that behavior.
   *
   * Package name is enforced identical by agp across multiple source sets, so taking the first
   * package name we find is fine.
   */
  private fun BaseVariant.packageName(project: Project): String {
    return sourceSets.map { it.manifestFile }
        .filter { it.exists() }
        .mapNotNull {
          DefaultManifestParser(it, { true }, SyncIssueHandlerImpl(STANDARD, project.logger))
              .`package`
        }
        .first()
  }

  private fun addMigrationTasks(
    project: Project,
    sourceSet: Collection<File>,
    schemaOutputDirectory: File?
  ) {
    val verifyMigrationTask = project.tasks.create("verifySqlDelightMigration", VerifyMigrationTask::class.java) {
      it.sourceFolders = sourceSet
      it.source(sourceSet)
      it.include("**${File.separatorChar}*.${SqlDelightFileType.defaultExtension}")
      it.include("**${File.separatorChar}*.${MigrationFileType.defaultExtension}")
    }

    verifyMigrationTask.group = "sqldelight"
    verifyMigrationTask.description = "Verify SQLDelight migrations and CREATE statements match."

    if (schemaOutputDirectory != null) {
      val generateSchemaTask =
        project.tasks.create("generateSqlDelightSchema", GenerateSchemaTask::class.java) {
          it.sourceFolders = sourceSet
          it.outputDirectory = schemaOutputDirectory
          it.source(sourceSet)
          it.include("**${File.separatorChar}*.${SqlDelightFileType.defaultExtension}")
          it.include("**${File.separatorChar}*.${MigrationFileType.defaultExtension}")
        }
      generateSchemaTask.group = "sqldelight"
      generateSchemaTask.description = "Generate a .db file containing the current schema."
    }

    project.tasks.findByName("check")?.dependsOn(verifyMigrationTask)
  }

  // Copied from kotlin plugin
  private val SourceSet.kotlin: SourceDirectorySet?
    get() {
      val convention = (getConvention("kotlin") ?: getConvention("kotlin2js")) ?: return null
      val kotlinSourceSetIface = convention.javaClass.interfaces.find { it.name == KotlinSourceSet::class.qualifiedName }
      val getKotlin = kotlinSourceSetIface?.methods?.find { it.name == "getKotlin" } ?: return null
      return getKotlin(convention) as? SourceDirectorySet

    }

  private fun Any.getConvention(name: String): Any? =
    (this as HasConvention).convention.plugins[name]
}
