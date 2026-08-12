import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources"
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.animefavorites.kmp"
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
            baseName = "anime-favorites"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.mvikotlin.extensions.coroutines)

            implementation(project(":feature-kmp:anime-base"))
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":core-kmp:network"))
            implementation(project(":core-kmp:anime-database"))
            implementation(project(":core-kmp:di-scope"))
            implementation(project(":feature-kmp:anime-background-update"))

            implementation(libs.mvikotlin)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.essenty.lifecycle)
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)

            implementation(libs.kotlin.inject.runtime)
            implementation(libs.kotlin.inject.anvil.runtime)
            implementation(libs.kotlin.inject.anvil.runtime.optional)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.material)
            implementation(libs.androidx.swiperefreshlayout)
            implementation(libs.androidx.recyclerview)
            implementation(libs.glide)
            implementation(libs.androidx.work.runtime)
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":feature-kmp:anime-base"))
            implementation(project(":feature-kmp:anime-background-update"))
        }
    }
}

dependencies {
    val kspTargets = listOf("Android", "IosArm64", "IosSimulatorArm64")
    kspTargets.forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
        add("ksp$target", libs.kotlin.inject.anvil.compiler)
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
