---
paths:
  - "**/src/androidHostTest/**"
  - "**/src/androidDeviceTest/**"
  - "**/androidApp/src/test/**"
  - "**/androidApp/src/androidTest/**"
---

# Tests on Android

- Before writing or changing an `androidHostTest` or an `androidApp` host test, a composable test
  or an instrumented test, read
  [.claude/design/testing-platforms.md](../../design/testing-platforms.md), section "Android". It
  holds where composable tests run, the module setup they need, what a host test may boot, and how
  the SDK Robolectric emulates is set.
- An instrumented test on a device is the furthest a test may go, and only where a host test
  genuinely cannot reach. It is never the first tool reached for. One that launches the real app
  needs the developer's permission; see [../tests-on-device.md](../tests-on-device.md).
- No automated test runs against the minified build; the manual walk in
  [r8-minified.md](r8-minified.md) covers it. A test APK there would use the app's shrunk copies of
  the shared libraries, and R8 removes what only the test needs. Making it pass would take keep
  rules that change what ships.
