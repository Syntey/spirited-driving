plugins {
    id(libs.plugins.conventions.kmp.feature.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // The distance unit comes from the phone's region until the user picks one
                implementation(projects.core.platform)
                // Trip history groups drives by month and day in the phone's time zone
                implementation(libs.kotlinx.dateTime)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
