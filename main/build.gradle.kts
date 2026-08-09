import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alekseivinogradov.anoti.main"
    //noinspection GradleDependency
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
    }
}

dependencies {
    implementation(project(":feature-kmp:anime-base"))
    implementation(project(":core-platform:celebrity"))
    implementation(project(":core-platform:navigation"))
    implementation(project(":core-kmp:network"))
    implementation(project(":ui-core:res"))
    implementation(project(":core-platform:anime-database"))
    implementation(project(":core-platform:di"))
    implementation(project(":feature-kmp:bottom-navigation-bar"))
    implementation(project(":feature-platform:anime-list"))
    implementation(project(":feature-kmp:anime-favorites"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.mvikotlin)
    implementation(libs.essenty.lifecycle)
    implementation(libs.dagger)
    implementation(libs.androidx.constraintlayout)
    ksp(libs.dagger.compiler)
}
