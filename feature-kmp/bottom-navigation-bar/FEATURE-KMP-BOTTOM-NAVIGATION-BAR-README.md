The app's bottom navigation bar: an MVI store tracking the selected section and the favorites
badge count.

## Entities

- [BottomNavigationBarStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/api/domain/store/BottomNavigationBarStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.
- [BottomNavigationBarView](src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/presentation/BottomNavigationBarView.kt) —
  the view contract the platform layer implements to render the store's state.
- [BottomNavigationBarController](src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/presentation/BottomNavigationBarController.kt) —
  wires the store to its view and to `AnimeDatabaseStore`.

## How to include it

- Gradle: `implementation(project(":feature-kmp:bottom-navigation-bar"))`
- `BottomNavigationBarStore`'s binding is provided by this module's commonMain
  `DiBottomNavigationBarComponent` and mixed into `main`'s `DiRootComponent`, the app's
  `RootScope` component — inject it, don't construct it yourself.
  `BottomNavigationBarView` has no DI wiring; the consumer implements it directly (see `main`'s
  `BottomNavigationBarViewImpl`). `BottomNavigationBarController` has no DI wiring either;
  construct it directly with the store and lifecycle.

## How to use it

Implement `BottomNavigationBarView` (a `BottomNavigationBarViewImpl`): render `UiModel` in
`render()`, call `dispatch(Intent)` from the relevant UI callbacks (tab clicks), and handle
navigation in `handle(Label)`. On the screen hosting the bar, construct
`BottomNavigationBarController` with the store, `AnimeDatabaseStore`, and the screen's lifecycle,
then call `controller.onViewCreated(viewImpl, viewLifecycle)`.
