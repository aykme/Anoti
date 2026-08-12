plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.alekseivinogradov.anoti.navigation"
    //noinspection GradleDependency
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
dependencies {
    implementation(project(":feature-kmp:anime-list"))
    implementation(project(":feature-kmp:anime-favorites"))
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}
