import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.detekt)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.alekseivinogradov.anoti.celebrity.kmp.generated.resources"
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.celebrity.kmp"
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
            baseName = "celebrity"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core-kmp:network"))
            implementation(project(":core-kmp:di-scope"))
            // Each of these appears in this module's own public signatures: coroutine contexts
            // and flows, Compose State, Modifier, Color, TextUnit and ColorScheme, a
            // StringResource, and MVIKotlin's view types.
            api(libs.kotlinx.coroutines.core)
            api(libs.compose.runtime)
            api(libs.compose.components.resources)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            api(libs.mvikotlin)

            implementation(libs.kotlinx.datetime)
            // Only the store factory this module's own DI bindings build.
            implementation(libs.mvikotlin.main)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}
