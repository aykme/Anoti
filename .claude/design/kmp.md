# Kotlin Multiplatform

How the project uses Kotlin Multiplatform: the targets every module builds for, how a KMP module
declares its Android target, which source set code belongs in, when `expect`/`actual` is used, the
one iOS framework, how Compose resources reach each platform, and what Kotlin/Native constrains.
It owns placement; source set names and directories are in [module-anatomy.md](module-anatomy.md).

Read when: adding Kotlin code or deciding where it lives; adding platform-specific code; adding
an `expect` declaration; changing a module's framework or Compose-compiler setup; adding Compose
resources; writing code or tests that run on Kotlin/Native.

## Targets

Every KMP module builds the same three targets:

- `android`: the Android app. It compiles to the JVM at the catalog's `jvmTarget`.
- `iosArm64`: iOS devices.
- `iosSimulatorArm64`: the simulator on Apple silicon.

There is no Intel simulator target. The Xcode project excludes `x86_64` for the simulator SDK,
and Compose's resource task refuses that slice.
Example: [iosApp/project.yml](../../iosApp/project.yml).

A module declares all three even when it holds no code for one platform.
Example: [anime-notification-external build file](../../feature-kmp/anime-notification-external/build.gradle.kts).

The Android target comes from the AGP KMP library plugin,
`com.android.kotlin.multiplatform.library` (catalog alias `androidKotlinMultiplatformLibrary`).
It is configured inside `kotlin { android { } }`, not in a top-level `android { }` block:

- `namespace`, `compileSdk` and `minSdk` from the catalog, and `compilerOptions { jvmTarget }`.
- `withHostTestBuilder {}` turns on `androidHostTest`; `withDeviceTestBuilder {}` turns on
  `androidDeviceTest`.
- `androidResources` where the module has Compose resources; see
  [module-anatomy.md "Manifests and resources"](module-anatomy.md#manifests-and-resources).

The plugin builds one variant, with no build types. Build types exist only in the Android host.
Example: [anime-list build file](../../feature-kmp/anime-list/build.gradle.kts).
The full module boilerplate is in [new-module.md](new-module.md), section "Skeletons".

## Where code lives

`commonMain` is the default for everything, and a platform source set is only for code impossible
to write there. That default, and the check before adding to a platform source set, are owned by
[source-sets.md](../rules/source-sets.md). This section holds the rest of the placement.

- One exception, for iOS only. Code that exists only because iOS lacks a mechanism Android's OS
  provides lives in `iosMain`, even when it is portable. The entry module's iOS root holder, root
  content, saved-state codec and child lifecycle are the case. They keep the screen state Android
  keeps in its saved instance state, and rebuild the root from it. Logic both platforms share
  stays in `commonMain`, whoever calls it.
  Example: [IosRootHolder](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/IosRootHolder.kt),
  [IosRootContent](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/IosRootContent.kt),
  [SaveableStateCodec](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/savedstate/SaveableStateCodec.kt),
  [ChildLifecycle](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/lifecycle/ChildLifecycle.kt).
- This applies to Compose code too. A composable only needs `androidMain`/`iosMain` if it directly
  touches a platform-only API (e.g. a `View`/`ComposeView` bridge). A composable built entirely
  from `compose.runtime`/`compose.foundation`/`compose.material3` and other `commonMain` types
  belongs in `commonMain`, regardless of which platform currently calls it.
- When portable logic needs a platform-specific value or condition (e.g. an Android-only
  OS-version check), compute it in the platform layer and pass the *result* in as a plain
  parameter (a `Boolean`, a `Modifier`, a `Dp`). Don't let the platform concept itself (its name,
  its reasoning) leak into the `commonMain` signature.
- Where both platforms need the same thing built (wording, an id, a format), build it once in
  `commonMain` and have both call it. Two copies drift, and review is not what should hold them
  together.
  Example: [newEpisodeNotificationText](../../feature-kmp/anime-notification/src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/kmp/impl/presentation/manager/NewEpisodeNotificationText.kt).

## Expect and actual

`expect`/`actual` is used in three places:

- The platform context: an `expect abstract class` that is Android's `Context` through an
  `actual typealias`, and an empty abstract class on iOS. It lets DI wiring stay in `commonMain`.
  Example: [PlatformContext](../../core-kmp/di-scope/src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/PlatformContext.kt).
- Room's database constructor: an `expect object` whose `actual`s Room's KSP processor writes.
  Example: [AnimeDatabase](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/data/AnimeDatabase.kt).
- Component creators: an `@KmpComponentCreate expect fun createDi<Name>Component(...)`, whose body
  kotlin-inject's KSP processor writes per target. How creators are used is in
  [dependency-injection.md](dependency-injection.md).
  Example: [DiRootComponent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/di/DiRootComponent.kt).

Everything else that differs per platform is an interface in `commonMain` with one implementation
per platform. Each platform's platform component binds its implementation; see
[dependency-injection.md](dependency-injection.md), section "Module and platform components".
Example: [AnimeNotificationManager](../../feature-kmp/anime-notification/src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/kmp/api/domain/manager/AnimeNotificationManager.kt).

A module that declares an `expect` class or object passes `-Xexpect-actual-classes` to the
compiler. Where that goes is in [new-module.md](new-module.md), section "Skeletons". File names of
`actual` declarations are in [module-anatomy.md](module-anatomy.md), section "Naming".

## The iOS framework

- One iOS framework exists, `Shared`. `:core-kmp:di-app` links it, since it sits above every other
  module.
- No other module declares a framework. A second one would bring a second copy of the Kotlin
  runtime and of the shared state.
- `core-kmp:di-app` applies the Compose plugins for the framework's sake. Compose copies the
  resources of the modules below into an app bundle through the module that links it.
- The Compose compiler is switched on for the native targets only in that module. On Android it
  would change the DI classes, and with them what R8 produces. Keep the restriction.

The framework is static and built for both iOS targets. Xcode's build phase builds it through
Gradle; the Xcode project is covered by [xcode-project.md](../rules/ios/xcode-project.md). What
the framework exposes to Swift is in [ios-host.md](ios-host.md).
Example: [di-app build file](../../core-kmp/di-app/build.gradle.kts).

## Resources

- Strings, drawables and fonts are Compose resources in `commonMain`. Both platforms read them
  through the module's generated `Res` class.
- On Android, a module's Compose resources reach the app through its Android target, which needs
  `androidResources` enabled.
- On iOS, the module that links the framework copies every module's resources into the app bundle.
- Platform resources hold only what each OS reads outside Compose. Android XML resources hold the
  launcher icons, the notification's small icon, the starting-window theme and the app name. The
  iOS asset catalog holds the app icon and the launch background.
  Example: [iosApp Assets.xcassets](../../iosApp/iosApp/Assets.xcassets).

The Gradle settings for `Res` are in [module-anatomy.md](module-anatomy.md), section "Manifests and
resources". Using resources in UI is in [ui-compose.md](ui-compose.md), section "Resources and
localization".

## Kotlin/Native constraints

- `commonTest` uses handwritten doubles and library mocks, with no general-purpose mocking
  framework; why is in [decisions.md "Deliberate absences"](decisions.md#deliberate-absences).
- Ktor's `MockEngine` needs the test's dispatcher to stay on the virtual clock; see
  [testing.md](testing.md), section "Virtual time".
- Kotlin that serves UIKit is called on the main thread only, and its KDoc says so. A delegate
  Apple calls on an unnamed queue hops to the main queue itself.
  Example: [NotificationTapDelegate](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/notification/NotificationTapDelegate.kt).
- Kover cannot measure Kotlin/Native, so `iosMain` stays outside the coverage number; see
  [test-coverage.md](../rules/test-coverage.md).
- An `iosTest` runs only on macOS; see [ios/tests.md](../rules/ios/tests.md).

## Compiling iOS off macOS

Off macOS, Kotlin/Native compiles the iOS targets but links and runs nothing. The local iOS check
and how to read its output are in [build-check.md](../rules/ios/build-check.md).
