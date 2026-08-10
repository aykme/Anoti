Cross-module Dagger component contracts that let feature modules reach the app's and the
main screen's dependency graphs without depending on `:app`/`:main` concrete classes.

## Entities

- [AppComponent](src/androidMain/kotlin/com/alekseivinogradov/anoti/di/android/api/presentation/app/AppComponent.kt) —
  the application-scoped component contract.
- [ApplicationExternal](src/androidMain/kotlin/com/alekseivinogradov/anoti/di/android/api/presentation/app/ApplicationExternal.kt) —
  exposes `AppComponent` from an `Application` reference.
- [MainComponent](src/androidMain/kotlin/com/alekseivinogradov/anoti/di/android/api/presentation/main/MainComponent.kt) —
  the "main" screen's activity-scoped component contract.
- [MainActivityExternal](src/androidMain/kotlin/com/alekseivinogradov/anoti/di/android/api/presentation/main/MainActivityExternal.kt) —
  exposes `MainComponent` from an `Activity` reference.

## How to include it

- Gradle: `implementation(project(":core-kmp:di"))`
- No DI wiring of its own — these are contracts, not providers. `:app`'s `Application` and
  `:main`'s `MainActivity` implement `ApplicationExternal`/`MainActivityExternal` and back their
  `@Component`-annotated internal components with `AppComponent`/`MainComponent`; feature modules
  read the component off the `Activity`/`Application` via those two `External` interfaces.
- The `@AppContext`/`@ActivityContext` qualifier annotations these contracts' methods carry live
  in `:core-kmp:celebrity`, not here (celebrity is the leaf every consumer already depends on, so
  hosting them there avoids a dependency cycle back into this module) — this module re-exports
  that dependency via `api`, so depending on `:core-kmp:di` alone is enough to see them too.

## How to use it

```kotlin
// Android example (no iOS example yet):
class SomeFragment : Fragment() {
    private fun createComponent() {
        val mainComponent = (this.activity as MainActivityExternal).mainComponent
        // Use mainComponent to build this feature's own Dagger component
    }
}
```
