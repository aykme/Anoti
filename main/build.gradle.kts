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
            baseName = "main"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `DiRootComponent`'s supertypes and accessors expose types from these three modules,
            // so they're part of this module's own API surface, not just an implementation detail.
            api(project(":feature-kmp:bottom-navigation-bar"))
            api(project(":feature-kmp:anime-list"))
            api(project(":feature-kmp:anime-favorites"))

            implementation(project(":feature-kmp:anime-base"))
            implementation(project(":feature-kmp:anime-background-update"))
            implementation(project(":feature-kmp:notifications-rationale-dialog"))
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":core-kmp:network"))
            implementation(project(":core-kmp:anime-database"))
            implementation(project(":core-kmp:di-scope"))
            implementation(project(":core-kmp:navigation"))

            implementation(libs.mvikotlin)
            implementation(libs.compose.runtime) // required once kotlinCompose is applied
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        androidMain.dependencies {
            implementation(project(":feature-kmp:anime-notification-external"))

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

dependencies {
    val kspTargets = listOf("Android", "IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
    }

    // Renders ComposeView content in Android Studio's layout preview (activity_main.xml);
    // androidRuntimeClasspath (not debugImplementation) is what
    // com.android.kotlin.multiplatform.library expects it on.
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

// Lint's androidHostTest-related tasks read kspAndroidHostTest's generated sources without
// Gradle inferring that dependency on its own, so the full aggregate `build` can schedule them
// first (Gradle's own implicit-dependency validation flags exactly this).
tasks.matching {
    it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest"
}.configureEach {
    dependsOn("kspAndroidHostTest")
}
