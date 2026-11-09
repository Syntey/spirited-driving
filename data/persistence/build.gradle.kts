plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    id(libs.plugins.conventions.koin.get().pluginId)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.logging.kermit)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.datastore.preferences.core)

                implementation(projects.core.domain)
            }
        }
    }
}
