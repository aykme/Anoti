# Testing on each platform

How the platform test source sets are set up and what tests in them may do: Android host tests
with Robolectric and Compose, and the iOS tests of Kotlin/Native code. The general shape of a test
is in [.claude/design/testing.md](testing.md).

Read when: writing an `androidHostTest`, a composable test or an `iosTest`; giving a module host
tests; testing `iosMain` code.

## Android

- Composables get tests too, but not from `commonTest`: `runComposeUiTest` compiles there and then
  fails at runtime on the Android host test. They belong in `androidHostTest`, driven by
  Robolectric and `androidx.compose.ui.test.junit4.v2.createComposeRule`. The non-`v2` rule is
  deprecated. v2 defaults to `StandardTestDispatcher`, so coroutines need the scheduler advanced.
  Example:
  [SystemMessageHostTest](../../core-kmp/celebrity/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/SystemMessageHostTest.kt).
- The module needs `robolectric` and `compose-ui-test-junit4` in that source set, both already in
  the version catalog, plus `withHostTestBuilder {}.configure { isIncludeAndroidResources = true }`.
  Without the merged resources the rendered screens find neither their theme nor their Compose
  resources. Add `compose-ui-test-manifest` only where the rule has to launch its own host
  activity; a test that launches the module's own activity does not need it. Example:
  [the anime-list build file](../../feature-kmp/anime-list/build.gradle.kts).
- A host test never boots the real app. It stays on the JVM with Robolectric standing in for the
  framework, and never uses the app's own `Application`; it supplies a stub of its own. The one
  exception is the `androidApp` module, where that `Application` is the subject. Robolectric
  creates it there and the test drives it directly, with every background service it reaches still
  faked. Example:
  [HostApplicationFake](../../main/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/HostApplicationFake.kt).
  Example:
  [AnotiAppTest](../../androidApp/src/test/kotlin/com/alekseivinogradov/anoti/impl/presentation/AnotiAppTest.kt).
- The SDK Robolectric emulates is set for the whole project, from `robolectricSdk` in the version
  catalog: the root build writes it into a `robolectric.properties` on each module's host-test
  classpath. Don't put `@Config(sdk = ...)` on a test. It belongs only on a class that genuinely
  needs a different level, and then it says why. Name the level through the generated `MIN_SDK`
  where that is the one it needs; the root build writes that constant from the catalog too, since
  an annotation cannot read one. Example:
  [MainActivityNotificationSettingsTest](../../main/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivityNotificationSettingsTest.kt).
- Left to itself Robolectric targets `compileSdk` and dies inside `ApplicationSharedMemory.create`,
  which it cannot emulate. The message it prints blames the JRE rather than the SDK level.

How the root build generates those files is in
[.claude/design/build-and-tooling.md](build-and-tooling.md), section "Root build".

Device tests:

- When a test may go to a device, and what launching the real app needs, is in
  [android/tests.md](../rules/android/tests.md) and
  [tests-on-device.md](../rules/tests-on-device.md).
- Where a module keeps its instrumented tests and how they are enabled and run is in
  [android/instrumented-tests.md](../rules/android/instrumented-tests.md). Example:
  [the anime-notification build file](../../feature-kmp/anime-notification/build.gradle.kts).
- Why no automated test runs against the minified build is in
  [android/tests.md](../rules/android/tests.md).

## iOS

Where an `iosTest` runs, and what it counts for off macOS, is in
[ios/tests.md](../rules/ios/tests.md).

- Before writing an `iosTest`, check whether the code under it needs an iOS API at all. A class
  built from coroutines, atomics and the module's own types belongs in `commonMain`, where its test
  runs on every build and is measured, even when iOS is its only caller. The portable half of a
  background refresh shows the shape: it is common and tested, and the iOS scheduler is left
  holding the `BGTaskScheduler` calls and nothing else. Example:
  [BackgroundRefreshPass](../../feature-kmp/anime-background-update/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/domain/scheduler/BackgroundRefreshPass.kt).
  Example:
  [BackgroundRefreshTask](../../feature-kmp/anime-background-update/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/domain/scheduler/BackgroundRefreshTask.kt).
  Example:
  [AnimeBackgroundSchedulerImpl on iOS](../../feature-kmp/anime-background-update/src/iosMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/ios/impl/domain/scheduler/AnimeBackgroundSchedulerImpl.kt).
- The iOS-only code of [.claude/design/kmp.md](kmp.md#where-code-lives), section "Where code
  lives", is tested in `iosTest`, and stays outside the coverage number. Where its tests are
  written first is in [ios/tests.md](../rules/ios/tests.md).
- What stays in `iosTest` is what only a real iOS runtime can answer. It is written knowing it runs
  only in `ios.yml`, never off macOS.
- An `iosTest` builds no UIKit window or view. The test process has no app, so UIKit starts only
  halfway, and the Compose tests in the same process then crash on text input.

Compose UI that only iOS shows is tested in `iosTest` with `runComposeUiTest`, from the
`compose-ui-test` library in that source set. Example:
[IosRootContentTest](../../main/src/iosTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/IosRootContentTest.kt).

The Swift UI tests in `iosApp/iosAppUITests` launch the real app. When they may be written, and
how they retry, is in [tests-on-device.md](../rules/tests-on-device.md); how CI runs them is in
[ios/ci.md](../rules/ios/ci.md).
