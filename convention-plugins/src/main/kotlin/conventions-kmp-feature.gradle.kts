import org.gradle.accessors.dm.LibrariesForLibs

/*
A feature module: its screens' components, Compose UI and iOS view controllers. It may depend only on `:core:*` modules
(see `moduleGraphAssert` in the root build script), and gets all of them but `:core:platform` from here.

Arkitekt generates internal component factories, so a navigation host in `:shared` can't call them. Each feature
exposes its screens through a public object instead (see `SampleScreens`).
 */
plugins {
    id("conventions-compose")
    id("conventions-arkitekt")
}

val libs = the<LibrariesForLibs>()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:domain"))
                implementation(project(":core:resources"))
                implementation(project(":core:ui"))

                implementation(libs.jetbrains.compose.material.icons)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
                implementation(libs.moko.resources.compose)
            }
        }
    }
}
