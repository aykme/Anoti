plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alekseivinogradov.main"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":core-platform:celebrity"))
    implementation(project(":core-platform:navigation"))
    implementation(project(":ui-core:res"))
    implementation(project(":core-platform:anime-database"))
    implementation(project(":core-platform:di"))
    implementation(project(":feature-kmp:bottom-navigation-bar"))
    implementation(project(":feature-platform:anime-list"))
    implementation(project(":feature-platform:anime-favorites"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.mvikotlin)
    implementation(libs.mvikotlin.main)
    implementation(libs.dagger)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.ktx)
    ksp(libs.dagger.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
