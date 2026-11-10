pluginManagement {
    // Convention plugins and the build's shared configuration (`ProjectSettings`, `ProductFlavors`, …)
    includeBuild("convention-plugins")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "SpiritedDriving"

// https://docs.gradle.org/8.1.1/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":androidApp")
include(":baselineprofile")

// Shared code. Which module may depend on which is checked by `moduleGraphAssert` in the root build script.
include(":shared")

include(":core:domain")
include(":core:platform")
include(":core:resources")
include(":core:ui")

include(":data:network")
include(":data:persistence")

include(":feature:sample")

// Architecture rules, see `konsist/build.gradle.kts`
include(":konsist")
