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
    // :androidApp -> :shared -> :feature:* or :data:* -> :core:*, with room for one :core:* module using another
    maxHeight = 4
    configurations = setOf("api", "implementation") +
        listOf("commonMain", "commonTest", "androidMain", "iosMain").flatMap { listOf("${it}Api", "${it}Implementation") }
    assertOnAnyBuild = true
}

// Keeps the hand-grouped order and the `project-*-namespace` versions no dependency references.
versionCatalogUpdate {
    sortByKey.set(false)
    keep {
        keepUnusedVersions.set(true)
    }
}
