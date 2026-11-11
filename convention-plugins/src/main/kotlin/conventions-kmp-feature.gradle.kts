import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/*
A feature module: its screens' components, Compose UI and iOS view controllers. It may depend only on `:core:*` modules
(see `moduleGraphAssert` in the root build script), and gets all of them but `:core:platform` from here.

Arkitekt's KSP processor generates a factory for each component annotated with `@GenerateFactory`. The factories are
internal, so a navigation host in `:shared` can't call them. Each feature exposes its screens through a public object
instead (see `OnboardingScreens`).
 */
plugins {
    id("conventions-compose")
    id("conventions-arkitekt")
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
                implementation(project(":core:domain"))
                implementation(project(":core:resources"))
                implementation(project(":core:ui"))

                implementation(libs.futured.arkitekt.decomposeAnnotation)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
                implementation(libs.moko.resources.compose)
            }
        }
    }
}
