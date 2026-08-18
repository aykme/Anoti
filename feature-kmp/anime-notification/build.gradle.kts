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
        //noinspection GradleDependency
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        androidResources {
            enable = true
        }

        withJava()

        withHostTestBuilder {}.configure {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "anime-notification"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-kmp:di-scope"))
            implementation(project(":core-kmp:celebrity"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.glide)
            api(project(":feature-kmp:anime-notification-external"))
        }
    }
}
