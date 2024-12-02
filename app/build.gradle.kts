plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alekseivinogradov.anoti"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.alekseivinogradov.anoti"
        minSdk = libs.versions.minSdk.get().toInt()
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(project(":ui-core:res"))
    implementation(project(":core-platform:celebrity"))
    implementation(project(":core-platform:anime-database"))
    implementation(project(":core-platform:di"))
    implementation(project(":core-platform:network"))
    implementation(project(":feature-platform:anime-base"))
    implementation(project(":feature-platform:anime-background-update"))
    implementation(project(":feature-platform:anime-notification"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.mvikotlin)
    implementation(libs.mvikotlin.logger)
    implementation(libs.mvikotlin.timetravel)
    implementation(libs.dagger)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.services.appset)
    ksp(libs.dagger.compiler)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
