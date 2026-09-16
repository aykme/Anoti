Shared core utilities used across Anoti's KMP feature modules: coroutine contexts, date
formatting, error toasts, pagination, and Compose UI helpers for MVIKotlin-based screens.

## Entities

- [CoroutineContextProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/coroutinecontext/CoroutineContextProvider.kt) —
  coroutine contexts and dispatchers used across the app.
- [DateFormatter](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/formatter/DateFormatter.kt) —
  formats date strings for display.
- [ToastProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/toast/provider/ToastProvider.kt) —
  holds the error-toast callbacks.
- [ToastController](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/toast/controller/ToastController.kt) —
  app-wide stream of toast messages.
- [ToastHost](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/ToastHost.kt) —
  shows `ToastController`'s messages as snackbars.
- [Paginator](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/paging/Paginator.kt) —
  pages through loads one page at a time.
- [PageLoadResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/paging/PageLoadResult.kt) —
  outcome of a `Paginator` page load.
- [ComposeMviView](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/ComposeMviView.kt) —
  base `MviView` that renders a store's state into a Compose `State` instead of a real View.
- [Modifier.repeatingClickable](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/RepeatingClickable.kt) —
  press-and-hold-to-repeat click behavior for Compose.
- [LoadingSpinner](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/LoadingSpinner.kt) —
  continuously spinning loading indicator.
- [Modifier.horizontalSystemBarsPadding](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/SystemBarsInsets.kt) —
  insets content away from the left and right system bars.
- [systemBarsTopPadding](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/SystemBarsInsets.kt) —
  the space the system bars take at the top of the window.

## How to include it

- Gradle: `implementation(project(":core-kmp:celebrity"))`
- `CoroutineContextProvider`, `DateFormatter`, `ToastProvider` and `ToastController` are provided
  via this module's kotlin-inject bindings (`DiCelebrityComponent`), mixed into
  [`core-kmp:di-app`](../di-app/CORE-KMP-DI-APP-README.md)'s `DiAppComponent` on both platforms —
  inject them, don't construct them yourself. `ToastHost`, `Paginator`, `ComposeMviView`,
  `repeatingClickable`, `LoadingSpinner`, `horizontalSystemBarsPadding` and `systemBarsTopPadding`
  have no DI wiring; callers subclass/construct/call them directly.

## How to use it

```kotlin
// A feature reports an error:
toastProvider.makeConnectionErrorToast()

// The root Compose content shows it, placed last so it is drawn over the screen:
ToastHost(controller = dependencies.toastController)
```
