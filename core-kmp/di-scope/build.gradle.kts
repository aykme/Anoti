import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.alekseivinogradov.anoti.discope.kmp"
        //noinspection GradleDependency
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        withJava()

        withHostTestBuilder {}.configure {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "di-scope"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Only the plain @Qualifier annotation from kotlin-inject's runtime is used here
            // (in Qualifier.kt) — no kotlin-inject-anvil annotations appear in this module, so
            // its runtime/runtime-optional artifacts aren't pulled in.
            implementation(libs.kotlin.inject.runtime)
        }
    }
}
