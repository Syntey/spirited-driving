import com.syntey.spiriteddriving.gradle.configuration.ProjectSettings
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.accessors.dm.LibrariesForLibs
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

/*
This is a Precompiled Script Plugin that unifies lint
configuration across multiple Gradle modules in the project.

https://docs.gradle.org/current/userguide/custom_plugins.html#sec:precompiled_plugins
 */

// As of now, we cannot use Gradle version catalogs with Precompiled Script plugins: https://github.com/gradle/gradle/issues/15383
plugins {
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.detekt")
}

val libs = the<LibrariesForLibs>()

// Ktlint
project.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set(libs.versions.ktlint)
    ignoreFailures.set(false)
    android.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }

    // filtering does not work, see below 👇
}

/*
Detekt. Any issue fails the build (`failOnSeverity` defaults to `Error`, the severity of every rule).

detekt 2 registers a task per source set (light analysis: syntax only) and per Android compilation (full analysis:
type-aware rules too). `lintCheck` runs the ones in `ProjectSettings.Lint.DetektTasks`.
 */
project.configure<DetektExtension> {
    config.setFrom(files("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        sarif.required = false
        markdown.required = false
        checkstyle.required = true
    }

    // Source sets include generated code (KSP, moko-resources, BuildKonfig), which isn't ours to lint
    val buildDirectory = layout.buildDirectory.get().asFile.absolutePath
    exclude { it.file.absolutePath.startsWith(buildDirectory) }
}

// Common source sets include the KSP output, so detekt has to run after KSP
pluginManager.withPlugin("com.google.devtools.ksp") {
    tasks.withType<Detekt>().configureEach {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

val lintCheckDetektTasks = tasks.withType<Detekt>().matching { it.name in ProjectSettings.Lint.DetektTasks }

lintCheckDetektTasks.configureEach {
    finalizedBy(rootProject.tasks.named("detektReportMerge"))
}

/**
 * Configure task [runKtlintCheckOverCommonMainSourceSet] to run After KSP generation task
 * Setup this only if project has configured ksp and has [kspCommonMainKotlinMetadata] in it's tasks
 *
 * This is probably a bug on ktlint-gradle side. The filtering in KtlintExtension does not work so ktlint wants to also check generated files.
 * I managed to get everything working only by combining .editorconfig ktlint = disable trick with combination of this ugly monstrosity.
 *
 * Number of lost souls: 1
 * (Increase this number if you lost several hours of your life trying to resolve this issue)
 *
 * Refs:
 * - https://github.com/JLLeitschuh/ktlint-gradle/issues/746
 * - https://github.com/JLLeitschuh/ktlint-gradle/issues/751
 */
tasks.matching { it.name == "runKtlintCheckOverCommonMainSourceSet" }.configureEach {
    dependsOn(project.tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}

/**
 * This is ktlint-gradle bug workaround. Ktlint should **not** have to require formatting generated files, the filtering does not work
 * and suggested workarounds do not work as well. So here we are, generating sources using KSP so ktlint can be satisfied 🤠.
 *
 * Number of lost souls: 1
 * (Increase this number if you lost several hours of your life trying to resolve this issue)
 *
 * Refs:
 * - https://github.com/JLLeitschuh/ktlint-gradle/issues/724
 */
tasks.matching { it.name == "runKtlintFormatOverCommonMainSourceSet" }.configureEach {
    dependsOn(project.tasks.matching { it.name == "kspCommonMainKotlinMetadata" })
}

rootProject.tasks.named("detektReportMerge", ReportMergeTask::class.java) {
    // Files, not task outputs: the merge collects what ran, instead of pulling in every report's task
    input.from(lintCheckDetektTasks.map { it.reports.checkstyle.outputLocation.get().asFile })
}
