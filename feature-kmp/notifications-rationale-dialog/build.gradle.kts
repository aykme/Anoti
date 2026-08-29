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
        //noinspection GradleDependency
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        androidResources {
            enable = true
        }

        withHostTestBuilder {}.configure {}
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
            implementation(project(":core-kmp:celebrity"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
        }
    }
}

dependencies {
    // Renders @Preview composables in Android Studio; androidRuntimeClasspath (not
    // debugImplementation) is what com.android.kotlin.multiplatform.library expects it on.
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
