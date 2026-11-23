plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    id(libs.plugins.conventions.koin.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
