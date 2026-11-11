plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.koin.core)
                implementation(libs.koin.annotations)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
