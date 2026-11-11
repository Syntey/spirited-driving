plugins {
    id(libs.plugins.conventions.kmp.feature.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
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
