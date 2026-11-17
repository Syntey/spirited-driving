plugins {
    id(libs.plugins.conventions.kmp.feature.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.platform)
                implementation(libs.kotlinx.dateTime)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.google.maps.compose)
            }
        }
    }
}
