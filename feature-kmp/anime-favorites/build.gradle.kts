import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources"
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.animefavorites.kmp"
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
            baseName = "anime-favorites"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.mvikotlin.extensions.coroutines)

            // Each of these appears in this module's own public signatures: the screen
            // component's dependencies, its source and store, the controller's lifecycle and
            // database store, the navigation component's own supertype, and the Compose entry
            // points with their UI models.
            api(project(":feature-kmp:anime-background-update"))
            api(project(":feature-kmp:anime-base"))
            api(project(":core-kmp:celebrity"))
            api(project(":core-kmp:network"))
            api(project(":core-kmp:anime-database"))
            api(libs.mvikotlin)
            api(libs.kotlinx.collections.immutable)
            api(libs.essenty.lifecycle)
            api(libs.decompose)
            api(libs.compose.runtime)
            api(libs.compose.ui)

            implementation(project(":core-kmp:di-scope"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mvikotlin.main)
            implementation(libs.ktor.client.mock)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}

dependencies {
    val kspTargets = listOf("Android", "IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
    }

    // Renders @Preview composables in Android Studio; androidRuntimeClasspath (not
    // debugImplementation) is what com.android.kotlin.multiplatform.library expects it on.
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
