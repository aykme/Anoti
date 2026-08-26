Retry helpers for this app's instrumented UI tests, for both Compose Testing and Espresso.

## Entities

- [safeComposeInteraction](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/SafeComposeInteraction.kt) —
  retries a Compose Testing node lookup until it stops throwing.
- [safeInteraction](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/SafeInteraction.kt) —
  retries an Espresso interaction until it stops throwing.

## How to include it

- Gradle: `androidTestImplementation(project(":core-kmp:test-utils"))`
- No DI — call `safeComposeInteraction`/`safeInteraction` directly from test code.
