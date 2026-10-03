Retry helpers for this app's tests that run on a device.

## Entities

- [safeComposeInteraction](src/commonMain/kotlin/com/alekseivinogradov/anoti/testutils/kmp/api/compose/SafeComposeInteraction.kt) —
  retries a Compose Testing node lookup until it stops throwing.
- [RetryRule (Android)](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/api/rule/RetryRule.kt) —
  runs a failed test again, up to a set number of tries.

## How to include it

- Gradle: `androidTestImplementation(project(":core-kmp:test-utils"))`, or the same project in a
  multiplatform module's `androidDeviceTest` dependencies.
- No DI — call `safeComposeInteraction` directly from test code, and declare `RetryRule` as the
  test's outermost `@get:Rule`.
