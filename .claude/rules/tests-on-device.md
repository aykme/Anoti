---
paths:
  - "**/src/androidDeviceTest/**"
  - "**/androidApp/src/androidTest/**"
  - "**/iosApp/iosAppUITests/**"
  - "**/src/iosTest/**"
  - "**/.github/scripts/ios-restore-checks.sh"
  - "**/.github/scripts/ios-check-dark.py"
---

# Tests on a device, and tests that launch the real app

- A test that launches the real app is the last resort: a UI test, or a CI check that drives the
  installed app. It is written only where nothing smaller can prove the behavior, and only with
  the developer's permission. Its assertions stay on structure, never on values the live backend
  decides.
- Already allowed by the developer: Android's `AnimeFavoritesUserFlowTest`, and on iOS the UI tests
  and the restore checks. They reach the live backend.
- Every test that runs on a device or a simulator gets up to three tries, since a device can fail
  on its own and CI must not go red over it. Every failed try stays visible in the log.
- On Android, such a test takes `RetryRule` from `core-kmp:test-utils` as its outermost rule.
- On iOS, `ios.yml` retries the UI tests through `xcodebuild`, the Kotlin/Native tests by running
  their Gradle task again, and each restore case as a whole. Each UI test and restore case starts
  from a fresh installation of its own.
- So, as [.claude/design/testing.md](../design/testing.md) says in "Structure", a test passes on
  its own, in any order. It never leans on what another test or an earlier try left behind; a
  shared setup goes into a preparation step every test runs.
