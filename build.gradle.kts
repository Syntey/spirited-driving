plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.koin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.aboutlibraries.android) apply false
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.modules.graph.assert)

    id(libs.plugins.conventions.root.get().pluginId)
}

// Which module may depend on which, see "Modules" in README.md. Checked whenever the build is configured.
moduleGraphAssert {
    allowed = arrayOf(
        ":androidApp -> .*",
        ":shared -> .*",
        ":(core|data|feature):.* -> :core:.*",
    )
    // :core:ui uses only Compose: screens pass it their texts and icons, so a new string or icon doesn't rebuild the components
    restricted = arrayOf(":core:ui -X> :core:resources")
    // :androidApp -> :shared -> :feature:* or :data:* -> :core:*, with room for one :core:* module using another
    maxHeight = 4
    configurations = setOf("api", "implementation") +
        listOf("commonMain", "commonTest", "androidMain", "iosMain").flatMap { listOf("${it}Api", "${it}Implementation") }
    assertOnAnyBuild = true
}

// Architecture rules (see `konsist/build.gradle.kts`) are part of the static checks
tasks.named("lintCheck") {
    dependsOn(":konsist:test")
}

// Keeps the hand-grouped order and the `project-*-namespace` versions no dependency references.
versionCatalogUpdate {
    sortByKey.set(false)
    keep {
        keepUnusedVersions.set(true)
    }
    // Held back on purpose, see the comment on each version in the catalog
    pin {
        versions.add("koinCompilerPlugin")
    }
}
