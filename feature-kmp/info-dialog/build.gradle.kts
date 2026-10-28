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
    packageOfResClass = "com.alekseivinogradov.anoti.infodialog.kmp.generated.resources"
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.infodialog.kmp"
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

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Each of these appears in this module's own public signatures: the composable, the
            // store contract and its executor, and the module component's providers.
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.mvikotlin)
            api(libs.mvikotlin.extensions.coroutines)
            // The executor and the module component take the UI kit's coroutine context and
            // system message providers.
            api(project(":core-kmp:celebrity"))
            // The usecases wrapper and the module component name the web page usecase.
            api(project(":feature-kmp:web-page-opener"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlin.inject.runtime.kmp)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mvikotlin.main)
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
