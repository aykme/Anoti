---
paths:
  - "**/src/androidHostTest/**"
  - "**/src/androidDeviceTest/**"
  - "**/androidApp/src/test/**"
  - "**/androidApp/src/androidTest/**"
---

# Tests on Android

- Composables get tests too, but not from `commonTest`: `runComposeUiTest` compiles there and then
  fails at runtime on the Android host test. They belong in `androidHostTest`, driven by
  Robolectric and `androidx.compose.ui.test.junit4.v2.createComposeRule`. The non-`v2` rule is
  deprecated. v2 defaults to `StandardTestDispatcher`, so coroutines need the scheduler advanced.
- The module needs `robolectric` and `compose-ui-test-junit4` in that source set, both already in
  the version catalog, plus `withHostTestBuilder {}.configure { isIncludeAndroidResources = true }`.
  Without the merged resources the rendered screens find neither their theme nor their Compose
  resources. Add `compose-ui-test-manifest` only where the rule has to launch its own host
  activity; a test that launches the module's own activity does not need it.
- A host test never boots the real app. It stays on the JVM with Robolectric standing in for the
  framework, and never uses the app's own `Application`; it supplies a stub of its own. The one
  exception is the `androidApp` module, where that `Application` is the subject. Robolectric
  creates it there and the test drives it directly, with every background service it reaches still
  faked.
- An instrumented test on a device is the furthest a test may go, and only where a host test
  genuinely cannot reach. It is never the first tool reached for. One that launches the real app
  needs the developer's permission; see [../tests-on-device.md](../tests-on-device.md).
- The SDK Robolectric emulates is set for the whole project, from `robolectricSdk` in the version
  catalog: the root build writes it into a `robolectric.properties` on each module's host-test
  classpath. Don't put `@Config(sdk = ...)` on a test. It belongs only on a class that genuinely
  needs a different level, and then it says why. Name the level through the generated `MIN_SDK`
  where that is the one it needs; the root build writes that constant from the catalog too, since
  an annotation cannot read one.
- Left to itself Robolectric targets `compileSdk` and dies inside `ApplicationSharedMemory.create`,
  which it cannot emulate. The message it prints blames the JRE rather than the SDK level.
- No automated test runs against the minified build; the manual walk in
  [r8-minified.md](r8-minified.md) covers it. A test APK there would use the app's shrunk copies of
  the shared libraries, and R8 removes what only the test needs. Making it pass would take keep
  rules that change what ships.
