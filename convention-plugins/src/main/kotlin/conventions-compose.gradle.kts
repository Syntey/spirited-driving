import org.gradle.accessors.dm.LibrariesForLibs

/*
A shared module with Compose Multiplatform UI: the compiler plugin and the libraries every such module uses.
 */
plugins {
    id("conventions-kmp-library")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<LibrariesForLibs>()

dependencies {
    // Compose rules for Android lint, which runs on this module's code (see `conventions-kmp-library`)
    add("lintChecks", libs.lint.compose)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.jetbrains.compose.runtime)
                implementation(libs.jetbrains.compose.foundation)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.jetbrains.compose.ui.tooling.preview)
                implementation(libs.jetbrains.compose.material3)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))

                // Renders @Preview composables in Android Studio.
                implementation(libs.jetbrains.compose.ui.tooling)
            }
        }
    }
}
