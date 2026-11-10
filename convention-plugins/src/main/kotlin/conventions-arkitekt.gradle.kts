import org.gradle.accessors.dm.LibrariesForLibs

/*
Decompose components built with Arkitekt: features use it for their screens, `:shared` for its navigation hosts. Components
are resolved from Koin, and their configurations and arguments are saved as serialized state.

There's no code generation here. Only features generate component factories (see `conventions-kmp-feature`); `:shared`
creates its navigation hosts through `AppComponentFactory`. KSP on `:shared`'s common code would need every feature's common
and iOS code compiled first, in every build, including Android ones.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("conventions-koin")
}

val libs = the<LibrariesForLibs>()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.decompose)
                implementation(libs.futured.arkitekt.decompose)
                implementation(libs.futured.arkitekt.crUseCases)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.immutableCollections)
                implementation(libs.logging.kermit)
            }
        }
    }
}
