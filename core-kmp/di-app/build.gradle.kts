import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose) // required alongside composeMultiplatform
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

// Hosts both app-wide composition roots: `DiAppComponent` in androidMain is `:androidApp`'s
// Android root, and its twin in iosMain is the iOS root. Same class name, same package, one per
// platform source set. On iOS, `IosApp` is what Swift calls; it builds the component through
// `createDiAppComponent`, whose body KSP generates.
// It also links `Shared`, the project's one iOS framework, since it sits above every other
// module. The Compose plugins are applied for that: Compose copies the resources of the modules
// below into an app bundle through the module that links the framework.
kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.di.kmp"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The Android `DiAppComponent`'s supertypes and accessors expose types from these
            // modules, so they're part of this module's own API surface.
            api(project(":core-kmp:di-scope"))
            api(project(":core-kmp:network"))
            api(project(":core-kmp:celebrity"))
            api(project(":core-kmp:anime-database"))
            api(project(":feature-kmp:anime-base"))
            api(project(":feature-kmp:anime-background-update"))
            api(project(":feature-kmp:anime-notification"))
            api(project(":main"))

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        androidMain.dependencies {
            // `DiAnimeBackgroundUpdatePlatformComponent`'s `@Provides` function references
            // `WorkManager` in its signature; KSP needs the type resolvable while processing it.
            implementation(libs.androidx.work.runtime)
        }
    }
}

// This module holds no composable code. On Android the compiler would only add stability
// members to the DI classes, which changes what R8 produces.
composeCompiler {
    targetKotlinPlatforms.set(setOf(KotlinPlatformType.native))
}

dependencies {
    val kspTargets = listOf("Android", "IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
    }
}
