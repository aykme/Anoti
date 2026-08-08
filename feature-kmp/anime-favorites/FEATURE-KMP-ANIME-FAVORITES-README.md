The favorites screen: an MVI store over the user's saved anime, with episode-viewed tracking
and per-item notification toggles.

## Entities

- [AnimeFavoritesMainStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/store/AnimeFavoritesMainStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.
- [AnimeFavoritesView](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/AnimeFavoritesView.kt) —
  the view contract the platform layer implements to render the store's state.
- [AnimeFavoritesController](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/AnimeFavoritesController.kt) —
  wires the store to its view and to `AnimeDatabaseStore`.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-favorites"))`
- `AnimeFavoritesMainStore` is provided via the platform module's Dagger setup
  (`feature-platform/anime-favorites`) — inject it, don't construct it yourself.
  `AnimeFavoritesView` has no DI wiring; the consumer implements it directly (see
  `feature-platform/anime-favorites`'s `AnimeFavoritesViewImpl`). `AnimeFavoritesController` has
  no DI wiring either; construct it directly with the store and lifecycle.

## How to use it

Implement `AnimeFavoritesView` (an `AnimeFavoritesViewImpl`): render `UiModel` in `render()` and
call `dispatch(Intent)` from the relevant UI callbacks (item clicks, episode-viewed buttons,
notification toggle). On the favorites screen, construct `AnimeFavoritesController` with the
store, `AnimeDatabaseStore`, and the screen's lifecycle, then call
`controller.onViewCreated(viewImpl, viewLifecycle)`.
