plugins {
    id(libs.plugins.conventions.kmp.feature.get().pluginId)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.shared.network.rest)
                implementation(projects.shared.persistence)
                implementation(projects.shared.kmpResources)
            }
        }
    }
}
