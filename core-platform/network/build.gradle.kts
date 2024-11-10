plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.alekseivinogradov.network_platform"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(project(":core-kmp:network"))
    api(libs.retrofit)
    api(libs.converter.moshi)
    implementation(libs.androidx.core.ktx)
    implementation(libs.moshi.kotlin)

    /**
     * Necessary dependencies for older android versions.
     * Without them, the crash "java.lang.NoSuchFieldError:Companion when using okhttp3"
     * happens during an Internet request, due to some kind of dependency conflict.
     * https://stackoverflow.com/questions/65828761/java-lang-nosuchfielderror-companion-when-using-okhttp3-and-selenium
     */
    // define a BOM and its version
    implementation(platform(libs.okhttp.bom))
    // define any required OkHttp artifacts without version
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
