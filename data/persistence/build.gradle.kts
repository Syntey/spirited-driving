plugins {
    id(libs.plugins.conventions.kmp.library.get().pluginId)
    id(libs.plugins.conventions.koin.get().pluginId)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.logging.kermit)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.datastore.preferences.core)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)

                implementation(projects.core.domain)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.robolectric)
            }
        }
    }
}

// The schema of each database version is exported here and committed; each later step adds a migration from it.
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
