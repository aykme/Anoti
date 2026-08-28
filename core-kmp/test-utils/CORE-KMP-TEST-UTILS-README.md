Retry helpers for this app's instrumented UI tests.

## Entities

- [safeComposeInteraction](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/SafeComposeInteraction.kt) —
  retries a Compose Testing node lookup until it stops throwing.

## How to include it

- Gradle: `androidTestImplementation(project(":core-kmp:test-utils"))`
- No DI — call `safeComposeInteraction` directly from test code.
