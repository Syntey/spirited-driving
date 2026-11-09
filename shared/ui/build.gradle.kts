plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
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
                implementation(libs.jetbrains.compose.material.icons)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
                implementation(libs.moko.resources.compose)

                implementation(libs.decompose)
                implementation(libs.decompose.compose.ext)
                implementation(libs.kotlinx.immutableCollections)
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.futured.arkitekt.decompose)
                implementation(projects.shared.feature)
                implementation(projects.shared.kmpResources)
            }
        }

        androidMain {
            dependencies {
                // Renders @Preview composables in Android Studio.
                implementation(libs.jetbrains.compose.ui.tooling)
            }
        }
    }
}
