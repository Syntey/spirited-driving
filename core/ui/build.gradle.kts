plugins {
    id(libs.plugins.conventions.compose.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.haze)

                implementation(libs.decompose)
                implementation(libs.decompose.compose.ext)
                implementation(libs.futured.arkitekt.decompose)
                implementation(libs.futured.arkitekt.crUseCases)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.haze.blur)
                implementation(libs.haze.blur.materials)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.haze.glass)
                implementation(libs.haze.glass.material3)
            }
        }
    }
}
