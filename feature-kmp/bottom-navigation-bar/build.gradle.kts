import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.detekt)
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.bottomnavigationbar.kmp"
        //noinspection GradleDependency
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        withHostTestBuilder {}.configure {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "bottom-navigation-bar"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":core-kmp:anime-database"))
            implementation(project(":core-kmp:di-scope"))

            implementation(libs.mvikotlin)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.essenty.lifecycle)
            implementation(libs.mvikotlin.extensions.coroutines)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
    }
}
