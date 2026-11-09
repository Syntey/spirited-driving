package com.syntey.spiriteddriving.gradle.configuration

import org.gradle.api.Project

/**
 * Kotlin package of a shared module, derived from its directory: `shared/network/rest` is
 * `com.syntey.spiriteddriving.network.rest`. The `shared` directory only groups the modules, so it is not part of the package.
 */
val Project.modulePackageName: String
    get() {
        val segments = projectDir.relativeTo(rootDir).invariantSeparatorsPath
            .split("/")
            .dropWhile { it == "shared" }
            .map { it.replace("-", "") }
        return (listOf(ProjectSettings.Kotlin.BasePackageName) + segments).joinToString(".")
    }

/**
 * Android namespace of a shared module: its [modulePackageName] with an `.android` suffix.
 */
val Project.androidNamespace: String
    get() = "$modulePackageName.android"
