import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

// Hosts both app-wide composition roots: `DiAppComponent` in androidMain is `:app`'s Android root,
// and its twin in iosMain is the iOS root an iOS host app would create. Same class name, same
// package, one per platform source set — no `expect`/`actual` needed since each compiles only for
// its own target.
kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.di.kmp"
        //noinspection GradleDependency
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
            baseName = "di-app"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `DiAppComponent`'s supertypes and accessors expose types from these modules, so
            // they're part of this module's own API surface, not just an implementation detail.
            api(project(":core-kmp:di-scope"))
            api(project(":core-kmp:network"))
            api(project(":core-kmp:celebrity"))
            api(project(":core-kmp:anime-database"))
            api(project(":feature-kmp:anime-base"))
            api(project(":feature-kmp:anime-background-update"))
            api(project(":feature-kmp:anime-notification"))
            api(project(":main"))

            // Aggregated Di*Component supertypes expose MVIKotlin Store types (e.g.
            // BottomNavigationBarStore) in their @Provides signatures; KSP needs the type
            // resolvable while processing DiAppComponent, even with no direct import here.
            implementation(libs.mvikotlin)

            implementation(libs.kotlin.inject.runtime.kmp)
        }
        androidMain.dependencies {
            // `DiAnimeBackgroundUpdatePlatformComponent`'s `@Provides` function references
            // `WorkManager` in its signature; KSP needs the type resolvable while processing it.
            implementation(libs.androidx.work.runtime)
        }
    }
}

dependencies {
    val kspTargets = listOf("Android", "IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
    }
}
