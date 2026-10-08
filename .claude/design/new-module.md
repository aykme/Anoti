# New module

How a Gradle module is added: its kind, its names, its line in `settings.gradle.kts`, its build
file and its wiring. What a module holds inside is in [module-anatomy.md](module-anatomy.md).

Read when: creating a Gradle module or choosing its kind; changing a module's build file or
`settings.gradle.kts`.

The [creating-a-module](../skills/creating-a-module/SKILL.md) skill orders these steps and its
script checks the finished module against its kind.

## Before the first file

A new module's path rules arrive only after its first file is written. Read these before the
first file:

- Source sets: [source-sets.md](../rules/source-sets.md),
  [kmp.md "Where code lives"](kmp.md#where-code-lives) and
  [kmp.md "The iOS framework"](kmp.md#the-ios-framework), which no new module declares.
- [module-anatomy.md](module-anatomy.md), sections "Source sets", "Packages", "Naming" and
  "Manifests and resources"; [dependency-injection.md](dependency-injection.md);
  [ui-compose.md "Design tokens"](ui-compose.md#design-tokens).
- Tests: [testing.md](testing.md), [testing-platforms.md "Android"](testing-platforms.md#android),
  [tests.md](../rules/tests.md), [android/tests.md](../rules/android/tests.md),
  [test-coverage.md](../rules/test-coverage.md). README and
  regression file: [module-docs.md](../rules/module-docs.md).
- A screen: [navigation.md "Adding a destination"](navigation.md#adding-a-destination) and
  [recipes.md "New screen feature"](recipes.md#new-screen-feature).

## Choose the kind

Check first that no module already does the job ([principles.md](principles.md)). Then the first
"yes" names the kind, as [project-structure.md "Module kinds"](project-structure.md#module-kinds)
describes it:

1. A destination of the root stack? A screen feature.
2. Beside the stack for the root's whole life, with a store of its own? A root-level element.
3. Only composables and tokens, its state held by whoever shows it? A UI-only component.
4. One common contract with an implementation per platform? A platform service, even when
   several features need it.
5. A service, models or composables several features need? A shared base module.
6. Something a feature needs that only the entry module can give? An external module.
7. Used by every layer (DI scopes, navigation, network, UI kit, persistence, tests)? A core module.

## Names

| Name                               | Form                                                                                                                         | Owner                                                                                    |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------|
| Gradle path                        | `:core-kmp:<name>` for a core module, `:feature-kmp:<name>` for every other kind; an external module's name ends `-external` | [project-structure.md "Top-level tree"](project-structure.md#top-level-tree)             |
| `<module-id>`, packages, namespace | `<name>` without hyphens; `<root-package>.<module-id>.<platform>.<visibility>…`; namespace `<root-package>.<module-id>.kmp`  | [module-anatomy.md "Packages"](module-anatomy.md#packages)                               |
| `Res` package                      | `<root-package>.<module-id>.kmp.generated.resources`                                                                         | [module-anatomy.md "Manifests and resources"](module-anatomy.md#manifests-and-resources) |
| README, regression file            | From the Gradle path                                                                                                         | [module-docs.md](../rules/module-docs.md)                                                |

## Settings

- Add one `include(":<group>:<name>")` line to
  [settings.gradle.kts](../../settings.gradle.kts), beside its group. Repositories are declared
  there once; a build file that declares its own fails the build.

## Skeletons

Kover and detekt come from the root build: [build-and-tooling.md](build-and-tooling.md#root-build).
A reason comment a mirror carries on a line is copied with the line.

(a) Core library without UI or KSP; the base the others add to:

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.detekt)
}
kotlin {
    android {
        namespace = "<root-package>.<module-id>.kmp"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions { jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get())) }
        withHostTestBuilder {}.configure {}
    }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            // <Name>'s public signature exposes <library>'s types.
            api(libs.<library>)
            implementation(project(":core-kmp:di-scope"))
            implementation(libs.kotlin.inject.runtime.kmp)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }
}
```

Mirrors: [navigation build file](../../core-kmp/navigation/build.gradle.kts),
[network build file](../../core-kmp/network/build.gradle.kts)

The `di-scope` and kotlin-inject lines come only with DI bindings, as in network; navigation has
none and leaves both out.

(b) Screen feature with Compose, resources, KSP and host tests; what it adds to (a):

```kotlin
plugins {
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.ksp)
}
kotlin {
    android {
        androidResources { enable = true }
        withHostTestBuilder {}.configure {
            // Robolectric resolves ComponentActivity only from the merged resources.
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            // Each of these appears in this module's own public signatures: <which>.
            api(libs.mvikotlin)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}
dependencies {
    listOf("Android", "IosArm64", "IosSimulatorArm64").forEach { target ->
        add("ksp$target", libs.kotlin.inject.compiler.ksp)
    }
    // Renders @Preview composables in Android Studio; androidRuntimeClasspath (not
    // debugImplementation) is what com.android.kotlin.multiplatform.library expects it on.
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
// Lint's androidHostTest-related tasks read kspAndroidHostTest's generated sources without
// Gradle inferring that dependency on its own, so the full aggregate `build` can schedule them
// first (Gradle's own implicit-dependency validation flags exactly this).
tasks.matching {
    it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest"
}.configureEach { dependsOn("kspAndroidHostTest") }
```

Mirrors: [anime-favorites build file](../../feature-kmp/anime-favorites/build.gradle.kts),
[anime-list build file](../../feature-kmp/anime-list/build.gradle.kts)

(c) Module with platform code and platform DI components; what it adds to (a):

```kotlin
kotlin {
    // only with an expect class or object
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }
    sourceSets {
        androidMain.dependencies { implementation(libs.<android-library>) }
        iosMain.dependencies { implementation(libs.<ios-library>) }
    }
}
```

Mirrors:
[anime-background-update build file](../../feature-kmp/anime-background-update/build.gradle.kts),
[network build file](../../core-kmp/network/build.gradle.kts);
the flag: [di-scope build file](../../core-kmp/di-scope/build.gradle.kts),
[anime-database build file](../../core-kmp/anime-database/build.gradle.kts)

(d) UI-only component: (b) without serialization, KSP, the Lint block and MVI; its `commonMain`:

```kotlin
commonMain.dependencies {
    // The composable is public and takes <types>, so a caller needs these to call it.
    api(libs.compose.runtime)
    api(libs.compose.ui)
    implementation(project(":core-kmp:celebrity"))
    implementation(libs.compose.components.resources)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
}
```

Mirrors:
[notifications-rationale-dialog build file](../../feature-kmp/notifications-rationale-dialog/build.gradle.kts)

## Boilerplate notes

- KSP, for all three targets, only where a `@Component` or Room lives; a module whose components
  are mixed into another graph has none. Example:
  [bottom-navigation-bar build file](../../feature-kmp/bottom-navigation-bar/build.gradle.kts).
- The Lint block of (b) in a KSP module with host tests: Lint reads `kspAndroidHostTest`'s output
  without Gradle knowing.
- `-Xexpect-actual-classes` only for an `expect` class or object, not an `expect fun` creator.
- The preview library and `androidRuntimeClasspath(libs.compose.ui.tooling)`: only with previews.
- `compose.resources`, `androidResources` and the manifest: see
  [module-anatomy.md "Manifests and resources"](module-anatomy.md#manifests-and-resources). A
  module without Compose resources sets neither, and most modules have no manifest.
- Host-test setup: [testing-platforms.md "Android"](testing-platforms.md#android); device tests:
  [android/instrumented-tests.md](../rules/android/instrumented-tests.md).
- Room: its plugin, `schemaDirectory`, `room.generateKotlin`, its compiler per target and the
  SQLite natives for host tests.
  Mirrors: [anime-database build file](../../core-kmp/anime-database/build.gradle.kts).
- `api(...)` with a comment naming the signatures that need it: [principles.md](principles.md).
  As found: six `api(...)` lines carry no comment.
  Example: [network build file](../../core-kmp/network/build.gradle.kts).
- New libraries: [tech-stack.md "Adding a library"](tech-stack.md#adding-a-library).

## Wiring

The consumer adds the dependency along
[project-structure.md "Dependency edges"](project-structure.md#dependency-edges). For app-wide
bindings the composition root depends on the module, and both app graph twins mix in its
components; a root-level element's component goes into the root graph instead. See
[dependency-injection.md](dependency-injection.md), section "Module and platform components".

## Checklist

- Kind, names, settings line, build file and wiring follow the sections above; code is in
  `commonMain` unless [source-sets.md](../rules/source-sets.md) allows otherwise.
- Tests, coverage, README, regression file and its full-regression link follow the rules listed
  in "Before the first file".
- Compose UI passes [compose-compiler-reports.md](../rules/compose-compiler-reports.md), the
  local iOS check in [build-check.md](../rules/ios/build-check.md) passes, and the task finishes
  as [finishing-a-task.md](../rules/finishing-a-task.md) says.
