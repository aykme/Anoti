import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.alekseivinogradov.anoti"
    //noinspection GradleDependency
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.alekseivinogradov.anoti"
        minSdk = libs.versions.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
    }
}

dependencies {
    implementation(project(":core-kmp:di-app"))
    implementation(project(":feature-kmp:anime-background-update"))
    implementation(project(":feature-kmp:anime-notification"))
    implementation(project(":main"))

    // No direct Kotlin usage, but required for the manifest's AD_ID permission (Google Play
    // review) — a past cleanup pass already dropped this as apparently unused and had to
    // restore it; do not remove without also removing that permission.
    implementation(libs.play.services.appset)

    androidTestImplementation(project(":core-kmp:test-utils"))
    androidTestImplementation(project(":feature-kmp:anime-favorites"))
    androidTestImplementation(libs.compose.components.resources)

    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.recyclerview)
}
