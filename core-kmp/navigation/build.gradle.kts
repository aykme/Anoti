import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
}

kotlin {
    android {
        namespace = "com.alekseivinogradov.anoti.navigation.kmp"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        withHostTestBuilder {}.configure {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // NavRootComponent's public API exposes ComponentContext and Value<ChildStack<..>>.
            api(libs.decompose)
            // `NavRootDeepLink` reads and writes the payload with it. It also pulls the
            // serialization BOM in: without it the module compiles its generated code against the
            // older core Decompose brings.
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
