import com.syntey.spiriteddriving.gradle.configuration.ProductFlavor
import com.syntey.spiriteddriving.gradle.configuration.ProductFlavors
import com.syntey.spiriteddriving.gradle.configuration.modulePackageName
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    id(libs.plugins.conventions.koin.get().pluginId)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

// Kept out of git: ANDROID_DEV_API_URL in local.properties points Android dev builds at the local backend on another machine, as from a phone on the same Wi-Fi.
val androidDevApiUrl: String? = rootProject.file("local.properties").takeIf { it.exists() }
    ?.let { file -> Properties().apply { file.inputStream().use { load(it) } }.getProperty("ANDROID_DEV_API_URL") }
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.also { require(it.endsWith("/")) { "ANDROID_DEV_API_URL must end with /, like http://192.168.1.20:8080/v1/" } }

fun ProductFlavor.androidApiUrl(): String = (if (this == ProductFlavors.Dev) androidDevApiUrl else null) ?: androidRestApiUrl

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.bundles.ktorfit)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.logging.kermit)

                implementation(projects.core.domain)
                implementation(projects.core.platform)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.network.ktor.client.mock)
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

    targetConfigs {
        create("android") {
            buildConfigField(STRING, "apiUrl", ProductFlavors.DEFAULT.androidApiUrl())
            buildConfigField(STRING, "ktorUserAgentVersion", libs.versions.ktor.get())
        }
    }

    listOf(
        ProductFlavors.Dev,
        ProductFlavors.Prod,
    ).forEach {
        defaultConfigs(flavor = it.name) {
            buildConfigField(STRING, "apiUrl", it.restApiUrl)
            buildConfigField(STRING, "ktorUserAgentVersion", libs.versions.ktor.get())
        }
        targetConfigs(flavor = it.name) {
            create("android") {
                buildConfigField(STRING, "apiUrl", it.androidApiUrl())
                buildConfigField(STRING, "ktorUserAgentVersion", libs.versions.ktor.get())
            }
        }
    }
}
