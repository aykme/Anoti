import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

ksp {
    arg("room.generateKotlin", "true")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.alekseivinogradov.anoti.animedatabase.kmp"
        //noinspection GradleDependency
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }

        withJava()

        withHostTestBuilder {}.configure {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "anime-database"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.mvikotlin.extensions.coroutines)
            api(libs.androidx.room.runtime)

            implementation(project(":core-kmp:celebrity"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.mvikotlin)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
        }
        androidMain.dependencies {
            implementation(libs.dagger)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspAndroid", libs.dagger.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

// androidHostTest runs the Android target's BundledSQLiteDriver on the host JVM (via Robolectric),
// not on a real device, so its normal System.loadLibrary(...)-from-APK loading path can't find the
// native SQLite binary. BundledSQLiteDriver's Android-target NativeLibraryLoader supports pointing
// it at an explicit native library file via two system properties as a documented escape hatch for
// exactly this case; the binary itself comes from Google's own androidx.sqlite:sqlite-bundled-jvm
// artifact (same androidx.sqlite release, built for host JVM/desktop use), matching this module's
// pinned `sqlite` version so the native ABI matches the Kotlin/JNI bindings actually compiled in.
val sqliteHostOs =
    System.getProperty("os.name").lowercase().let {
        when {
            it.contains("linux") -> "linux"
            it.contains("mac") -> "osx"
            it.contains("windows") -> "windows"
            else -> error("Unsupported OS for BundledSQLiteDriver host-test native: $it")
        }
    }
val sqliteHostArch =
    System.getProperty("os.arch").lowercase().let {
        when {
            it == "aarch64" -> "arm64"
            it.contains("arm") -> if (it.contains("64")) "arm64" else "arm32"
            it.contains("64") -> "x64"
            it.contains("86") -> "x86"
            else -> error("Unsupported architecture for BundledSQLiteDriver host-test native: $it")
        }
    }
val sqliteHostNativeFileName =
    if (sqliteHostOs == "windows") {
        "sqliteJni.dll"
    } else {
        "libsqliteJni.${if (sqliteHostOs == "osx") "dylib" else "so"}"
    }

val sqliteBundledJvmNatives: Configuration =
    configurations.create("sqliteBundledJvmNatives") {
        isCanBeConsumed = false
        isTransitive = false
    }

dependencies {
    sqliteBundledJvmNatives("androidx.sqlite:sqlite-bundled-jvm:${libs.versions.sqlite.get()}")
}

val extractSqliteNativeForHostTests: TaskProvider<Copy> =
    tasks.register<Copy>("extractSqliteNativeForHostTests") {
        description = "Extracts the host OS/arch BundledSQLiteDriver native library for " +
                "androidHostTest to load via system properties (Robolectric has no APK to " +
                "unpack it from)."
        from(provider { zipTree(sqliteBundledJvmNatives.singleFile) }) {
            include("natives/${sqliteHostOs}_$sqliteHostArch/$sqliteHostNativeFileName")
            eachFile { path = name }
            includeEmptyDirs = false
        }
        into(layout.buildDirectory.dir("sqliteHostTestNative"))
    }

tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }.configureEach {
    dependsOn(extractSqliteNativeForHostTests)
    systemProperty(
        "androidx.sqlite.driver.bundled.path",
        layout.buildDirectory.dir("sqliteHostTestNative").get().asFile.absolutePath
    )
    systemProperty("androidx.sqlite.driver.bundled.name", sqliteHostNativeFileName)
}

// Lint's androidHostTest-related tasks read kspAndroidHostTest's generated sources without
// Gradle inferring that dependency on its own, so the full aggregate `build` can schedule them
// first (Gradle's own implicit-dependency validation flags exactly this).
tasks.matching {
    it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest"
}.configureEach {
    dependsOn("kspAndroidHostTest")
}
