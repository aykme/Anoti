Espresso helpers for this app's instrumented UI tests.

## Entities

- [safeInteraction](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/SafeInteraction.kt) —
  retries an Espresso interaction until it stops throwing.

## How to include it

- Gradle: `androidTestImplementation(project(":core-kmp:test-utils"))`
- No DI — call `safeInteraction` directly from test code.
