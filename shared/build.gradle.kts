import com.syntey.spiriteddriving.gradle.configuration.ProjectSettings
import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.SuppressSkieWarning
import co.touchlab.skie.configuration.SuspendInterop
import dev.icerock.gradle.MRVisibility
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

/*
The only module that sees all others: features meet each other in its navigation hosts, and `:core:domain` interfaces
meet their `:data:*` implementations in its Koin graph. It is also the iOS framework.
 */
plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    id(libs.plugins.conventions.arkitekt.get().pluginId)
    alias(libs.plugins.skie)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        androidResources { enable = true }
    }

    val xcf = XCFramework(ProjectSettings.IOS.FrameworkName)

    // Both targets are always declared (by `conventions-kmp-library`) for source set compatibility
    val arm64 = iosArm64()
    val simArm64 = iosSimulatorArm64()

    // Only build framework binaries for the selected targets
    val kmpFrameworkTargets = project.findProperty(ProjectSettings.IOS.KmpFrameworkTargetsBuildProperty)?.toString().let { targets ->
        when (targets) {
            "device" -> listOf(arm64)
            "simulator" -> listOf(simArm64)
            else -> listOf(arm64, simArm64)
        }
    }

    // Controls whether the KMP XCFramework is built as static or dynamic.
    // Dynamic (false) is needed for SwiftUI previews in Xcode (Debug builds).
    // Static (true, default) is used for Beta and Release builds.
    // Controlled via -PisStatic=true|false Gradle property, set from KMP_IS_STATIC in .xcconfig files.
    val isStaticFramework = project.findProperty(ProjectSettings.IOS.IsStaticFrameworkProperty)?.toString()?.toBoolean() ?: true

    kmpFrameworkTargets.forEach { target ->
        target.binaries.framework {
            baseName = ProjectSettings.IOS.FrameworkName
            binaryOptions += "bundleId" to ProjectSettings.IOS.FrameworkBundleId
            isStatic = isStaticFramework

            // Swift implements `PlatformBindings`, creates `DefaultAppComponentContext`, reads `MR` and hosts the features'
            // screens through their `*ScreenUiController` functions.
            export(projects.core.platform)
            export(projects.core.resources)
            export(projects.core.ui)
            export(projects.feature.onboarding)
            export(projects.feature.map)
            export(libs.futured.arkitekt.decompose)

            export(libs.decompose)
            export(libs.essenty)
            export(libs.kotlinx.immutableCollections)
            export(libs.moko.resources)

            xcf.add(this)
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.platform)
                implementation(projects.core.resources)
                implementation(projects.core.ui)

                implementation(projects.data.network)
                implementation(projects.data.persistence)

                implementation(projects.feature.onboarding)
                implementation(projects.feature.map)

                implementation(libs.logging.kermitCrashlytics)

                // Required for Compose compiler plugin, as this module links the features' Compose UI into the iOS framework
                implementation(libs.jetbrains.compose.runtime)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        iosMain {
            dependencies {
                api(projects.core.platform)
                api(projects.core.resources)
                api(projects.core.ui)
                api(projects.feature.onboarding)
                api(projects.feature.map)
                api(libs.futured.arkitekt.decompose)

                api(libs.decompose)
                api(libs.essenty)
                api(libs.kotlinx.immutableCollections)

                implementation(libs.logging.nsExceptionKt.core)
            }
        }
    }
}

skie {
    analytics {
        // https://skie.touchlab.co/Analytics
        disableUpload.set(false)
    }
    features {
        group {
            DefaultArgumentInterop.Enabled(false)
            SuspendInterop.Enabled(true)
            FlowInterop.Enabled(true)
            EnumInterop.Enabled(true)
            SealedInterop.Enabled(true)
            SuppressSkieWarning.NameCollision(true)
        }
    }
}

multiplatformResources {
    // This module uses the plugin just to export resources to iOS.
    // Visibility is set to Internal in order to prevent :androidApp
    // module to see duplicate resources from this module and :core:resources module.
    resourcesVisibility.set(MRVisibility.Internal)
    resourcesClassName.set("MR")
    iosBaseLocalizationRegion.set(ProjectSettings.IOS.MokoBaseLocalizationRegion)
}

private fun Copy.assembleAndCopySwiftPackageForBuildType(buildType: NativeBuildType) {
    group = ProjectSettings.Gradle.TaskGroup

    val frameworkName = ProjectSettings.IOS.FrameworkName
    val xcfDirectory = project.layout.buildDirectory.dir("XCFrameworks/${buildType.getName()}")
    val iosDirectory = rootProject.layout.projectDirectory.dir("iosApp/shared/KMP/Sources")

    doFirst {
        if (iosDirectory.asFile.exists()) {
            iosDirectory.asFile.deleteRecursively()
        }
    }

    // Produces assembleKMPDebugXCFramework or assembleKMPReleaseXCFramework depending on input build type
    val xcfTask = buildString {
        append("assemble")
        append(frameworkName.replaceFirstChar { it.titlecase() })
        append(buildType.getName().replaceFirstChar { it.titlecase() })
        append("XCFramework")
    }

    dependsOn(xcfTask)
    from(xcfDirectory)
    into(iosDirectory)
}

tasks.register<Copy>("assembleAndCopyDebugSwiftPackage") {
    assembleAndCopySwiftPackageForBuildType(NativeBuildType.DEBUG)
}

tasks.register<Copy>("assembleAndCopyReleaseSwiftPackage") {
    assembleAndCopySwiftPackageForBuildType(NativeBuildType.RELEASE)
}
