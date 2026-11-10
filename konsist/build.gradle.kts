import com.syntey.spiriteddriving.gradle.configuration.ProjectSettings

/*
Architecture rules the compiler and detekt can't check, written as Konsist tests. `lintCheck` runs them.

Konsist parses the source files of every module straight from disk, so this module depends on none of them.
 */
plugins {
    kotlin("jvm")
    id(libs.plugins.conventions.lint.get().pluginId)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(ProjectSettings.Kotlin.JvmToolchainVersion)
}

tasks.test {
    // The sources the rules read aren't on any classpath, so they're declared here for the task to rerun when they change.
    inputs.files(
        fileTree(rootDir) {
            include("**/src/**/*.kt")
            exclude("**/build/**", "**/.*/**")
        },
    ).withPathSensitivity(PathSensitivity.RELATIVE)
}
