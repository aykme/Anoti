import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose) // required alongside composeMultiplatform, see Task 1 step 3
    alias(libs.plugins.ksp)
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.alekseivinogradov.anoti.main.generated.resources"
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
            implementation(libs.compose.runtime) // required once kotlinCompose is applied
            implementation(libs.compose.components.resources)
        }
        androidMain.dependencies {
            implementation(project(":navigation"))
            implementation(project(":feature-kmp:anime-base"))
            implementation(project(":core-kmp:celebrity"))
            implementation(project(":core-kmp:network"))
            implementation(project(":core-kmp:anime-database"))
            implementation(project(":core-kmp:di"))
            implementation(project(":feature-kmp:bottom-navigation-bar"))
            implementation(project(":feature-kmp:anime-list"))
            implementation(project(":feature-kmp:anime-favorites"))
            implementation(project(":feature-kmp:anime-notification-external"))

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity)
            implementation(libs.mvikotlin)
            implementation(libs.essenty.lifecycle)
            implementation(libs.dagger)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.androidx.navigation.fragment.ktx)
            implementation(libs.androidx.navigation.ui.ktx)
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
