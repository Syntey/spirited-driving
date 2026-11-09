import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/*
A feature module: Decompose components built with Arkitekt, whose KSP processor generates code into common code.
 */
plugins {
    id("conventions-kmp-library")
    id("conventions-koin")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
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
                implementation(libs.jetbrains.compose.runtime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.immutableCollections)
                implementation(libs.logging.kermit)
            }
        }

        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
            }
        }
    }
}
