import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
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
            //put your multiplatform dependencies here
        }
        androidMain.dependencies {
            implementation(libs.dagger)
            implementation(libs.androidx.core.ktx)
            implementation(libs.glide)
            api(project(":feature-platform:anime-notification-external"))
            implementation(project(":core-platform:celebrity"))
            implementation(project(":core-platform:di"))
            implementation(project(":ui-core:res"))
        }
    }
}

dependencies {
    add("kspAndroid", libs.dagger.compiler)
}

// Lint's androidHostTest-related tasks read kspAndroidHostTest's generated sources without
// Gradle inferring that dependency on its own, so the full aggregate `build` can schedule them
// first (Gradle's own implicit-dependency validation flags exactly this).
tasks.matching {
    it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest"
}.configureEach {
    dependsOn("kspAndroidHostTest")
}
