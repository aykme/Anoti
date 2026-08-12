plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
}

// iOS-only module: `IosAppGraph` is the iOS app-scope graph, and Android's equivalent lives in
// `:app`. There is deliberately no Android target here — nothing on Android depends on this
// module anymore.
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "di"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-kmp:di-scope"))
            implementation(project(":core-kmp:network"))
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":core-kmp:anime-database"))
            implementation(project(":feature-kmp:anime-base"))
            implementation(project(":feature-kmp:anime-background-update"))
            implementation(project(":feature-kmp:anime-notification"))

            implementation(libs.mvikotlin)

            implementation(libs.kotlin.inject.runtime)
            implementation(libs.kotlin.inject.anvil.runtime)
            implementation(libs.kotlin.inject.anvil.runtime.optional)
        }
    }
}

dependencies {
    val kspTargets = listOf("IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
        add("ksp$target", libs.kotlin.inject.anvil.compiler)
    }
}
