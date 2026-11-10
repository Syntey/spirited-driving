import org.gradle.accessors.dm.LibrariesForLibs

/*
Koin with its compiler plugin, which builds the dependency graph from annotations (`@Module`, `@Factory`, …).
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.insert-koin.compiler.plugin")
}

val libs = the<LibrariesForLibs>()

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.koin.core)
                implementation(libs.koin.annotations)
            }
        }
    }
}

koinCompiler {
    userLogs.set(false)
}
