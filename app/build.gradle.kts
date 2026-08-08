import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
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
    implementation(project(":ui-core:res"))
    implementation(project(":core-platform:celebrity"))
    implementation(project(":core-platform:anime-database"))
    implementation(project(":core-platform:di"))
    implementation(project(":core-kmp:network"))
    implementation(project(":feature-platform:anime-base"))
    implementation(project(":feature-platform:anime-background-update"))
    implementation(project(":feature-platform:anime-notification"))
    androidTestImplementation(project(":main"))
    androidTestImplementation(project(":core-platform:test-utils"))

    implementation(libs.mvikotlin)
    implementation(libs.dagger)
    implementation(libs.androidx.work.runtime)
    implementation(libs.play.services.appset)
    implementation(libs.ktor.client.okhttp)

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
    ksp(libs.dagger.compiler)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.recyclerview)
}
