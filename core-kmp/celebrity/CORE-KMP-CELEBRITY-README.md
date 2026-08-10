Shared core utilities used across Anoti's KMP feature modules. Its `androidMain` also hosts the
app's shared Android resources (theme, launcher icons, common drawables/fonts) and the Dagger
qualifier/scope annotations (`@AppContext`, `@ActivityScope`, etc.) — both live here because this
is the one module every consumer already depends on, which keeps the Gradle dependency graph
acyclic. Neither is listed below since this README indexes `commonMain` only.

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
  own androidMain Dagger setup — inject them, don't construct them yourself. `Paginator` has no
  DI wiring; callers construct it directly.
