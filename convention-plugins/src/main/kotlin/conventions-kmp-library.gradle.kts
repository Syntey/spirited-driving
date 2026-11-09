import com.syntey.spiriteddriving.gradle.configuration.ProjectSettings
import com.syntey.spiriteddriving.gradle.configuration.androidNamespace
import org.gradle.accessors.dm.LibrariesForLibs

/*
Base of every shared KMP module: targets, SDK levels, Android namespace, tests and lint.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("conventions-lint")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain(ProjectSettings.Kotlin.JvmToolchainVersion)

    // Turns off warnings about beta feature https://youtrack.jetbrains.com/issue/KT-61573
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    android {
        namespace = project.androidNamespace
        compileSdk = ProjectSettings.Android.CompileSdkVersion
        minSdk = ProjectSettings.Android.MinSdkVersion

        // Runs `commonTest` on the JVM (`testAndroidHostTest`), which is what CI runs.
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
