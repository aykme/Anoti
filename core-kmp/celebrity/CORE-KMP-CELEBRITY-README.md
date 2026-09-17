Shared core utilities used across Anoti's KMP feature modules: coroutine contexts, date
formatting, error system messages, pagination, and Compose UI helpers for MVIKotlin-based screens.

## Entities

- [CoroutineContextProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/coroutinecontext/CoroutineContextProvider.kt) —
  coroutine contexts and dispatchers used across the app.
- [DateFormatter](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/formatter/DateFormatter.kt) —
  formats date strings for display.
- [SystemMessageProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/systemmessage/provider/SystemMessageProvider.kt) —
  holds the error system message callbacks.
- [SystemMessageController](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/systemmessage/controller/SystemMessageController.kt) —
  app-wide stream of system messages.
- [SystemMessageHost](src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/SystemMessageHost.kt) —
  shows `SystemMessageController`'s messages as snackbars.
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
- `CoroutineContextProvider`, `DateFormatter`, `SystemMessageProvider` and
  `SystemMessageController` are provided via this module's kotlin-inject bindings
  (`DiCelebrityComponent`), mixed into
  [`core-kmp:di-app`](../di-app/CORE-KMP-DI-APP-README.md)'s `DiAppComponent` on both platforms —
  inject them, don't construct them yourself. `SystemMessageHost`, `Paginator`, `ComposeMviView`,
  `repeatingClickable`, `LoadingSpinner`, `horizontalSystemBarsPadding` and `systemBarsTopPadding`
  have no DI wiring; callers subclass/construct/call them directly.

## How to use it

```kotlin
// A feature reports an error:
systemMessageProvider.makeConnectionErrorSystemMessage()

// The root Compose content shows it last, over the screen. The caller positions it:
SystemMessageHost(
    controller = dependencies.systemMessageController,
    modifier = Modifier.aboveBottomBarAndKeyboard(bottomBarHeight)
)
```
