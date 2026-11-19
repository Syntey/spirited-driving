plugins {
    id(libs.plugins.conventions.kmp.feature.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // The distance unit comes from the phone's region until the user picks one
                implementation(projects.core.platform)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
