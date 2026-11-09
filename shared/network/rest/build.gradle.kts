import com.syntey.spiriteddriving.gradle.configuration.ProductFlavors
import com.syntey.spiriteddriving.gradle.configuration.modulePackageName
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    id(libs.plugins.conventions.koin.get().pluginId)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktorfit)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.logging.kermit)

                implementation(projects.shared.platform)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.network.ktor.client.engine.okhttp)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.network.ktor.client.engine.darwin)
            }
        }
    }
}

buildkonfig {
    packageName = project.modulePackageName
    objectName = "FlavorConstants"

    defaultConfigs {
        buildConfigField(STRING, "apiUrl", ProductFlavors.DEFAULT.restApiUrl)
        buildConfigField(STRING, "ktorUserAgentVersion", libs.versions.ktor.get())
    }

    listOf(
        ProductFlavors.Dev,
        ProductFlavors.Prod,
    ).forEach {
        defaultConfigs(flavor = it.name) {
            buildConfigField(STRING, "apiUrl", it.restApiUrl)
            buildConfigField(STRING, "ktorUserAgentVersion", libs.versions.ktor.get())
        }
    }
}
