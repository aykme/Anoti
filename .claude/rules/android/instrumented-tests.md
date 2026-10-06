---
paths:
  - "**/.claude/rules/android/instrumented-tests.md"
  - "**/src/androidDeviceTest/**"
  - "**/androidApp/src/androidTest/**"
---

# Instrumented tests and the emulator

- The instrumented tests are part of running every test, on every task, not only one that touched
  the UI. The one exception is a task that changes no logic; see
  [../finishing-a-task.md](../finishing-a-task.md).
- They need a device, so `./gradlew allTests :androidApp:testDebugUnitTest` never reaches them. Two
  tasks run them: `./gradlew :androidApp:connectedDebugAndroidTest` and
  `./gradlew :feature-kmp:anime-notification:connectedAndroidDeviceTest`. The first drives the app
  against the live backend, so the emulator needs a connection. The second posts through the
  device's own notification service. Report their result with the rest.
- A multiplatform module keeps its instrumented tests in `src/androidDeviceTest/kotlin`, enabled by
  `withDeviceTestBuilder`. A module that gains them adds its `connectedAndroidDeviceTest` to the
  tasks above.
- They run on an emulator, never on the developer's phone; see
  [CLAUDE-ANDROID.md](CLAUDE-ANDROID.md).
- Before running UI (instrumented/`androidTest`) tests, always do a clean installation: uninstall
  the app from the emulator before installing and running, so a stale build doesn't mask a failure
  or fake a pass.
