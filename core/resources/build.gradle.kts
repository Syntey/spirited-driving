import com.syntey.spiriteddriving.gradle.configuration.ProjectSettings
import com.syntey.spiriteddriving.gradle.configuration.modulePackageName

plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.moko.resources)
}

kotlin {
    android {
        androidResources { enable = true }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.moko.resources)
                implementation(libs.jetbrains.compose.runtime)
                implementation(libs.kotlinx.dateTime)
                // Measures are formatted from the domain's lengths and distance units
                implementation(projects.core.domain)
            }
        }
        androidMain {
            dependencies {
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.compose.foundation)
            }
        }
    }
}

multiplatformResources {
    resourcesPackage.set(project.modulePackageName)
    resourcesClassName.set("MR")
    iosBaseLocalizationRegion.set(ProjectSettings.IOS.MokoBaseLocalizationRegion)
}
