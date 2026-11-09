plugins {
    id(libs.plugins.conventions.compose.get().pluginId)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.decompose)
                implementation(libs.decompose.compose.ext)
                implementation(libs.futured.arkitekt.decompose)
                implementation(libs.futured.arkitekt.crUseCases)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
