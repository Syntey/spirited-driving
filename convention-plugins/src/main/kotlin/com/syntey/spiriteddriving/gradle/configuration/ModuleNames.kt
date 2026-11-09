package com.syntey.spiriteddriving.gradle.configuration

import org.gradle.api.Project

/**
 * Kotlin package of a shared module, derived from its path: `:data:network` is `com.syntey.spiriteddriving.data.network`.
 */
val Project.modulePackageName: String
    get() {
        val segments = path.split(":")
            .filter { it.isNotEmpty() }
            .map { it.replace("-", "") }
        return (listOf(ProjectSettings.Kotlin.BasePackageName) + segments).joinToString(".")
    }

/**
 * Android namespace of a shared module: its [modulePackageName] with an `.android` suffix.
 */
val Project.androidNamespace: String
    get() = "$modulePackageName.android"
