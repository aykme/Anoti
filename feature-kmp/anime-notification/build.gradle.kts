import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.detekt)
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.alekseivinogradov.anoti.animenotification.kmp.generated.resources"
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.animenotification.kmp"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        androidResources {
            enable = true
        }

        withHostTestBuilder {}.configure {
            // Robolectric reads this module's Compose resources only from the merged ones.
            isIncludeAndroidResources = true
        }

        // The notification service applies posts on its own schedule, which only a device shows.
        withDeviceTestBuilder {}.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Both appear in the platform DI components' own public signatures: the scope and
            // qualifier annotations their bindings carry, and the coroutine context provider.
            api(project(":core-kmp:di-scope"))
            api(project(":core-kmp:celebrity"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.components.resources)
            implementation(libs.coil)
            implementation(libs.coil.network.ktor3)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.rules)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.compose.ui)
            api(project(":feature-kmp:anime-notification-external"))
        }
    }
}

// Compose copies the device test's resources into a folder the host test's asset merge reads as
// well, without Gradle seeing that dependency on its own.
tasks.matching { it.name == "mergeAndroidHostTestAssets" }.configureEach {
    dependsOn("copyAndroidDeviceTestComposeResourcesToAndroidAssets")
}
