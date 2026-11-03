import com.syntey.spiriteddriving.gradle.task.LintCheckTask
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
    // Apply plugins at the top level but don't apply to this project
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.koin) apply false
    alias(libs.plugins.version.catalog.update)
}

// Keeps the hand-grouped order and the `project-*-namespace` versions no dependency references.
versionCatalogUpdate {
    sortByKey.set(false)
    keep {
        keepUnusedVersions.set(true)
    }
}

tasks.register<LintCheckTask>("lintCheck")
tasks.register<ReportMergeTask>("detektReportMerge") {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merged.xml"))
}
