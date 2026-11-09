import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/*
Decompose components built with Arkitekt, whose KSP processor generates their factories into common code. Features use
it for their screens, `:shared` for its navigation hosts. The factories resolve components from Koin, and component
configurations and arguments are saved as serialized state.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("conventions-koin")
}

val libs = the<LibrariesForLibs>()

dependencies {
    add("kspCommonMainMetadata", libs.futured.arkitekt.decomposeProcessor)
}

// Platform KSP tasks (e.g. `kspAndroidMain`) read the common KSP output as well, so they need the same dependency as compilations.
tasks.matching { it.name.startsWith("ksp") || it is KotlinCompilationTask<*> }.configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(libs.decompose)
                implementation(libs.futured.arkitekt.decompose)
                implementation(libs.futured.arkitekt.decomposeAnnotation)
                implementation(libs.futured.arkitekt.crUseCases)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.immutableCollections)
                implementation(libs.logging.kermit)
            }
        }
    }
}
