import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose) // required alongside composeMultiplatform, see Task 1 step 3
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.main"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        withHostTestBuilder {}.configure {
            // The host tests render the real screens, and those resolve their theme and their
            // Compose resources only from the merged ones.
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "main"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `DiRootDependencies`, `DiRootComponent` and `NavRootChild` are public and name
            // types from all of these, so they're part of this module's own API surface.
            api(project(":feature-kmp:bottom-navigation-bar"))
            api(project(":feature-kmp:anime-list"))
            api(project(":feature-kmp:anime-favorites"))
            api(project(":feature-kmp:anime-base"))
            api(project(":feature-kmp:anime-background-update"))
            api(project(":core-kmp:celebrity"))
            api(project(":core-kmp:network"))
            api(project(":core-kmp:anime-database"))
            api(project(":core-kmp:navigation"))
            api(libs.mvikotlin)

            implementation(project(":feature-kmp:notifications-rationale-dialog"))
            implementation(project(":core-kmp:di-scope"))
            implementation(libs.compose.runtime) // required once kotlinCompose is applied
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        androidMain.dependencies {
            implementation(project(":feature-kmp:anime-notification-external"))

            implementation(libs.androidx.core)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.serialization.json)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mvikotlin.main)
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
        }
    }
}

dependencies {
    val kspTargets = listOf("Android", "IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
    }
}

// Lint's androidHostTest-related tasks read kspAndroidHostTest's generated sources without
// Gradle inferring that dependency on its own, so the full aggregate `build` can schedule them
// first (Gradle's own implicit-dependency validation flags exactly this).
tasks.matching {
    it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest"
}.configureEach {
    dependsOn("kspAndroidHostTest")
}
