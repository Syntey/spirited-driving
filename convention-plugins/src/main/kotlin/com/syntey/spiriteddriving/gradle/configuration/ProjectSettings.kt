package com.syntey.spiriteddriving.gradle.configuration

import org.gradle.api.JavaVersion

object ProjectSettings {

    object Gradle {
        const val TaskGroup = "futured"
    }

    object Lint {
        // The detekt tasks `lintCheck` runs: a full analysis of common and Android code (shared modules, app) and a light one of
        // iOS code, which detekt can only analyze fully for JVM and Android compilations
        val DetektTasks = setOf("detektMainAndroid", "detektRelease", "detektIosMainSourceSet")
    }

    object Kotlin {
        const val JvmToolchainVersion = 17

        // Shared modules derive their package from it, see `modulePackageName`.
        const val BasePackageName = "com.syntey.spiriteddriving"
    }

    object Android {
        // Android 12
        const val MinSdkVersion = 31
        const val TargetSdkVersion = 36

        // androidx.lifecycle 2.11.0 (pulled in by Compose Multiplatform) refuses to be compiled against anything lower.
        const val CompileSdkVersion = 37

        const val ApplicationId = "com.syntey.spiriteddriving.android"

        val JavaCompatibility = JavaVersion.VERSION_17
        const val KotlinJvmTargetNum = "17"

        object BuildTypes {
            const val Debug = "debug"
            const val Enterprise = "enterprise"
            const val Release = "release"

            val all = listOf(Debug, Enterprise, Release)
        }

        object Signing {
            object Debug {
                val StorePassword = "android"
                val KeyAlias = "androiddebugkey"
                val KeyPassword = "android"
            }
        }
    }

    object IOS {
        const val FrameworkName = "KMP"
        const val FrameworkBundleId = "com.syntey.spiriteddriving.shared"
        const val MokoBaseLocalizationRegion = "en"
        const val KmpFrameworkTargetsBuildProperty = "kmpFrameworkTargets"
        const val IsStaticFrameworkProperty = "isStatic"
    }
}
