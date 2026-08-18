Shared core utilities used across Anoti's KMP feature modules: coroutine contexts, date
formatting, error-toast callbacks, and pagination.

## Entities

- [CoroutineContextProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/coroutinecontext/CoroutineContextProvider.kt) —
  coroutine contexts and dispatchers used across the app.
- [DateFormatter](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/formatter/DateFormatter.kt) —
  formats date strings for display.
- [ToastProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/toast/provider/ToastProvider.kt) —
  holds the platform's error-toast callbacks.
- [Paginator](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/paging/Paginator.kt) —
  pages through loads one page at a time.
- [PageLoadResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/paging/PageLoadResult.kt) —
  outcome of a `Paginator` page load.

## How to include it

- Gradle: `implementation(project(":core-kmp:celebrity"))`
- `CoroutineContextProvider`, `DateFormatter` and `ToastProvider` are provided via this module's
  kotlin-inject bindings (`DiCelebrityComponent`, `DiCelebrityPlatformComponent`), mixed into
  `DiAppComponent` (`:app`'s on Android, [`core-kmp:di`](../di/CORE-KMP-DI-README.md)'s on iOS) —
  inject them, don't construct them yourself. `Paginator` has no DI wiring; callers construct it
  directly.
