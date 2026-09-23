import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
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
        // Debuggable is left off on purpose: AGP skips obfuscation for a debuggable variant, and
        // obfuscation is the part of R8 most likely to break something.
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // What release ships, signed with the debug key so it installs. Release carries no signing
        // config of its own, so it cannot be put on a device to walk the app through.
        create("minified") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
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
    implementation(project(":main"))

    // No direct Kotlin usage, but required for the manifest's AD_ID permission (Google Play
    // review) — a past cleanup pass already dropped this as apparently unused and had to
    // restore it; do not remove without also removing that permission.
    implementation(libs.play.services.appset)

    // `AnotiApp` implements WorkManager's `Configuration.Provider`. This also puts androidx.startup
    // on this module's compile classpath, so the manifest's InitializationProvider resolves.
    implementation(libs.androidx.work.runtime)

    // Only the host tests name these directly; the app graph already carries both.
    testImplementation(project(":feature-kmp:anime-background-update"))
    testImplementation(project(":feature-kmp:anime-notification"))
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    // Installs WorkManager in its test mode, so enqueuing records the request instead of
    // letting the update worker run and reach the network.
    testImplementation(libs.androidx.work.testing)

    androidTestImplementation(project(":core-kmp:test-utils"))
    androidTestImplementation(project(":feature-kmp:anime-favorites"))
    androidTestImplementation(libs.compose.components.resources)

    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Neither artifact is used from this module's code. Both arrive transitively at a version
    // that breaks the instrumented tests, so only their version is pinned here.
    constraints {
        androidTestImplementation(libs.androidx.espresso.core) {
            because(
                "Compose's ui-test ships Espresso 3.5.0, whose reflective " +
                    "InputManager.getInstance() API 37 removed"
            )
        }
        androidTestImplementation(libs.androidx.activity) {
            because(
                "Play Services drags in an activity version that no longer satisfies " +
                    "createAndroidComposeRule's ComponentActivity bound"
            )
        }
    }
}
