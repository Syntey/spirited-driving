plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.koin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.version.catalog.update)

    id(libs.plugins.conventions.root.get().pluginId)
}

// Keeps the hand-grouped order and the `project-*-namespace` versions no dependency references.
versionCatalogUpdate {
    sortByKey.set(false)
    keep {
        keepUnusedVersions.set(true)
    }
}
