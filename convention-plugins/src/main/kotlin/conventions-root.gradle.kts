import com.syntey.spiriteddriving.gradle.task.LintCheckTask
import dev.detekt.gradle.report.ReportMergeTask

/*
Applied to the root project only. Besides registering the tasks below, it puts this build's classes (`ProjectSettings`,
`ProductFlavors`, …) on the classpath of every module's build script.
 */

tasks.register<LintCheckTask>("lintCheck")
tasks.register<ReportMergeTask>("detektReportMerge") {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merged.xml"))
}
