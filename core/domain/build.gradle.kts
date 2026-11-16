plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // Settings observed as Flows, and the fakes that back them with state flows
                api(libs.kotlinx.coroutines.core)
            }
        }
    }
}
