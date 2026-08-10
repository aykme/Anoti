Espresso helpers for this app's instrumented UI tests.

## Entities

- [safeInteraction](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/SafeInteraction.kt) —
  retries an Espresso interaction until it stops throwing.
- [AtRecyclerPositionMatcher](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/matcher/AtRecyclerPositionMatcher.kt) —
  matches a `RecyclerView` item at a given adapter position.
- [clickOnChildView](src/androidMain/kotlin/com/alekseivinogradov/anoti/testutils/android/action/ClickOnChildView.kt) —
  an Espresso `ViewAction` that clicks a child view by id.

## How to include it

- Gradle: `androidTestImplementation(project(":core-kmp:test-utils"))`
- No DI — call `safeInteraction`/`clickOnChildView` and construct `AtRecyclerPositionMatcher`
  directly from test code.
