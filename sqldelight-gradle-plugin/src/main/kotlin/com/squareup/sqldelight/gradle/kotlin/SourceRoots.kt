package com.squareup.sqldelight.gradle.kotlin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.squareup.sqldelight.gradle.SqlDelightDatabase
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.io.File

/**
 * @return A list of source roots and their dependencies.
 *
 * Examples:
 *   Multiplatform Environment. Ios target labeled "ios".
 *     -> iosMain deps [commonMain]
 *
 *   Android environment. internal, production, release, debug variants.
 *     -> internalDebug deps [internal, debug, main]
 *     -> internalRelease deps [internal, release, main]
 *     -> productionDebug deps [production, debug, main]
 *     -> productionRelease deps [production, release, main]
 *
 *    Multiplatform environment with android target (oh boy)
 */
internal fun SqlDelightDatabase.sources(): List<Source> {
  // Multiplatform project.
  project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.let {
    return it.sources()
  }

  // Android project.
  project.extensions.findByName("android")?.let {
    return (it as BaseExtension).sources(project)
  }

  // Kotlin project.
  val sourceSets = (project.extensions.getByName("kotlin") as KotlinProjectExtension).sourceSets
  return listOf(
    Source(
      type = KotlinPlatformType.jvm,
      name = "main",
      sourceSets = listOf("main"),
      sourceDirectorySet = sourceSets.getByName("main").kotlin,
    ),
  )
}

private fun KotlinMultiplatformExtension.sources(): List<Source> {
  // For multiplatform we only support SQLDelight in commonMain
  return listOf(
    Source(
      type = KotlinPlatformType.common,
      nativePresetName = "common",
      name = "commonMain",
      variantName = null,
      sourceDirectorySet = sourceSets.getByName("commonMain").kotlin,
      sourceSets = listOf("commonMain"),
    )
  )
}

private fun BaseExtension.sources(project: Project): List<Source> {
  val variants: DomainObjectSet<out BaseVariant> = when (this) {
    is AppExtension -> applicationVariants
    is LibraryExtension -> libraryVariants
    else -> throw IllegalStateException("Unknown Android plugin $this")
  }
  val kotlinSourceSets = (project.extensions.getByName("kotlin") as KotlinProjectExtension).sourceSets
  val sourceSets = sourceSets
    .associate { sourceSet ->
      sourceSet.name to kotlinSourceSets.getByName(sourceSet.name).kotlin
    }

  return variants.map { variant ->
    Source(
      type = KotlinPlatformType.androidJvm,
      name = variant.name,
      variantName = variant.name,
      sourceDirectorySet = sourceSets[variant.name]!!,
      sourceSets = variant.sourceSets.map { it.name },
      registerGeneratedDirectory = { outputDirectoryProvider ->
        variant.addJavaSourceFoldersToModel(outputDirectoryProvider.get())
      }
    )
  }
}

private fun TaskContainer.namedOrNull(
  taskName: String
): TaskProvider<Task>? {
  return try {
    named(taskName)
  } catch (_: Exception) {
    null
  }
}

internal data class Source(
  val type: KotlinPlatformType,
  val nativePresetName: String? = null,
  val sourceDirectorySet: SourceDirectorySet,
  val name: String,
  val variantName: String? = null,
  val sourceSets: List<String>,
  val registerGeneratedDirectory: ((Provider<File>) -> Unit)? = null
) {
  fun closestMatch(sources: Collection<Source>): Source? {
    var matches = sources.filter {
      type == it.type || (type == KotlinPlatformType.androidJvm && it.type == KotlinPlatformType.jvm) || it.type == KotlinPlatformType.common
    }
    if (matches.size <= 1) return matches.singleOrNull()

    // Multiplatform native matched or android variants matched.
    matches = matches.filter {
      nativePresetName == it.nativePresetName && variantName == it.variantName
    }
    return matches.singleOrNull()
  }
}