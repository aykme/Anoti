plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "animeList"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature-kmp:anime-base"))
            implementation(project(":core-kmp:network"))
            implementation(project(":core-kmp:database"))

            api(libs.mvikotlin)
            api(libs.mvikotlin.main)
            api(libs.paging.common)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.mvikotlin.logger)
            implementation(libs.mvikotlin.timetravel)
            implementation(libs.mvikotlin.extensions.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.paging.testing)
        }
    }
}

android {
    namespace = "com.alekseivinogradov.animeList"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
