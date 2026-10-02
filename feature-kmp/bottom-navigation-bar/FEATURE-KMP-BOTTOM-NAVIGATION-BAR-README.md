The app's bottom navigation bar: an MVI store tracking the selected section and the favorites
badge count.

## Entities

- [BottomNavigationBarStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/api/domain/store/BottomNavigationBarStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.
- [BottomNavigationBarController](src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/presentation/BottomNavigationBarController.kt) —
  wires the store to `AnimeDatabaseStore` and hands the UI its state.
- [BottomNavigationBar](src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/presentation/compose/BottomNavigationBar.kt) —
  the Compose UI rendering `BottomNavigationBarUiModel`.

## How to include it

- Gradle: `implementation(project(":feature-kmp:bottom-navigation-bar"))`
- `BottomNavigationBarStore`'s binding is provided by this module's commonMain
  `DiBottomNavigationBarComponent` and mixed into `main`'s `DiRootComponent`, the app's
  `RootScope` component — inject it, don't construct it yourself.
  `BottomNavigationBarController` has no DI wiring; construct it directly with the stores, the
  host's lifecycle and a handler for the store's labels.

## How to use it

Construct `BottomNavigationBarController` once, with the store, `AnimeDatabaseStore`, the host's
lifecycle and a handler that switches screens on the store's navigation labels. Draw the
`BottomNavigationBar` composable from the controller's `state`, mapped to
`BottomNavigationBarUiModel`, and pass `controller::accept` as its `dispatch`. The store never
observes navigation on its own, so its host sends `ChangeSelectedSection` whenever the shown
screen changes.
