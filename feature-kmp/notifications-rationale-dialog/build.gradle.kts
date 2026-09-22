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
    packageOfResClass = "com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources"
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.notificationsrationaledialog.kmp"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        androidResources {
            enable = true
        }

        withHostTestBuilder {}.configure {
            // Robolectric resolves ComponentActivity only from the merged resources.
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "notifications-rationale-dialog"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The dialog is composable and its icon size is public, so a consumer needs both
            // the runtime to call it and the unit type to read that size.
            api(libs.compose.runtime)
            api(libs.compose.ui)

            implementation(project(":core-kmp:celebrity"))

            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}

dependencies {
    // Renders @Preview composables in Android Studio; androidRuntimeClasspath (not
    // debugImplementation) is what com.android.kotlin.multiplatform.library expects it on.
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
