plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "anime_list_kmp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature_kmp:anime_network_base"))

            api(libs.mvikotlin)
            api(libs.mvikotlin.main)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.mvikotlin.logger)
            implementation(libs.mvikotlin.timetravel)
            implementation(libs.mvikotlin.extensions.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.alekseivinogradov.anime_list_kmp"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
