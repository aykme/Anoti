import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.animelist.kmp"
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
            baseName = "anime-list"
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

            implementation(libs.mvikotlin)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.essenty.lifecycle)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mvikotlin.main)
        }
        androidMain.dependencies {
            implementation(libs.dagger)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.material)
            implementation(libs.androidx.swiperefreshlayout)
            implementation(libs.androidx.recyclerview)
            implementation(libs.glide)
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":core-kmp:di"))
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
