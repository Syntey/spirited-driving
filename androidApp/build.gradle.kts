import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import com.syntey.spiriteddriving.gradle.configuration.ProjectSettings
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.distribution)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.aboutlibraries.android)

    id(libs.plugins.conventions.lint.get().pluginId)
}

kotlin {
    jvmToolchain(ProjectSettings.Kotlin.JvmToolchainVersion)

    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(ProjectSettings.Android.KotlinJvmTargetNum))
    }
}

// Kept out of git: `MAPS_API_KEY` in local.properties, or the environment variable on CI. Without it the map stays blank.
val mapsApiKey: String = providers.environmentVariable("MAPS_API_KEY").orNull
    ?: rootProject.file("local.properties").takeIf { it.exists() }
        ?.let { file -> Properties().apply { file.inputStream().use { load(it) } }.getProperty("MAPS_API_KEY") }
    ?: ""

android {
    namespace = libs.versions.project.android.namespace.get()
    compileSdk = ProjectSettings.Android.CompileSdkVersion

    defaultConfig {
        applicationId = ProjectSettings.Android.ApplicationId
        minSdk = ProjectSettings.Android.MinSdkVersion
        targetSdk = ProjectSettings.Android.TargetSdkVersion
        versionCode = System.getenv("ANDROID_BUILD_NUMBER")?.toIntOrNull() ?: 1
        versionName = System.getenv("ANDROID_VERSION_NAME") ?: "1.x.x-local"
        manifestPlaceholders["mapsApiKey"] = mapsApiKey
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        getByName(ProjectSettings.Android.BuildTypes.Debug) {
            storeFile = file("keystore/debug.keystore")
            storePassword = ProjectSettings.Android.Signing.Debug.StorePassword
            keyAlias = ProjectSettings.Android.Signing.Debug.KeyAlias
            keyPassword = ProjectSettings.Android.Signing.Debug.KeyPassword
        }
        create(ProjectSettings.Android.BuildTypes.Release) {
            storeFile = file("keystore/todo_your_release_keystore.keystore")
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD").orEmpty()
            keyAlias = System.getenv("ANDROID_KEY_ALIAS").orEmpty()
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD").orEmpty()
        }
    }

    buildTypes {
        getByName(ProjectSettings.Android.BuildTypes.Debug) {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
        create(ProjectSettings.Android.BuildTypes.Enterprise) {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile(file("proguard-rules.pro"))

            signingConfig = signingConfigs.getByName(ProjectSettings.Android.BuildTypes.Debug)
            matchingFallbacks.add(ProjectSettings.Android.BuildTypes.Release)

            applicationIdSuffix = ".enterprise"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
        }
        getByName(ProjectSettings.Android.BuildTypes.Release) {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile(file("proguard-rules.pro"))

            signingConfig = signingConfigs.getByName(ProjectSettings.Android.BuildTypes.Release)
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = ProjectSettings.Android.JavaCompatibility
        targetCompatibility = ProjectSettings.Android.JavaCompatibility
    }

    lint {
        explainIssues = false // HTML/XML reports are too verbose in console logs
        checkDependencies = false // Required to get all unused resource from other modules (disabled to speed up linting)
        checkTestSources = true // Also check test case code for lint issues
        checkReleaseBuilds = false // If we run a full lint analysis as build part in CI, we can skip redundant checks
    }
}

// google-services.json is gitignored, so a fresh clone builds without Firebase
googleServices {
    missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN
}

dependencies {
    coreLibraryDesugaring(libs.androidTools.desugarLibs)
    lintChecks(libs.lint.compose)

    implementation(projects.shared)
    implementation(projects.core.platform)
    implementation(projects.core.resources)
    implementation(libs.moko.resources.compose)
    implementation(projects.core.ui)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.map)
    implementation(projects.feature.profile)
    implementation(projects.feature.routes)
    implementation(libs.futured.arkitekt.decompose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.work.runtime)

    implementation(libs.decompose)
    implementation(libs.decompose.compose.ext)

    implementation(libs.kotlinx.immutableCollections)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.koin.android)
    implementation(libs.google.playServices.location)

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.appCheck.playIntegrity)
    debugImplementation(libs.google.firebase.appCheck.debug)
    "enterpriseImplementation"(libs.google.firebase.appCheck.debug)
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.playServicesAuth)
    implementation(libs.google.identity.googleId)
    implementation(libs.logging.timber)

    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.baselineprofile)
}
