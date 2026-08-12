The favorites screen: an MVI store over the user's saved anime, with episode-viewed tracking
and per-item notification toggles.

## Entities

- [AnimeFavoritesMainStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/store/AnimeFavoritesMainStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.
- [AnimeFavoritesView](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/AnimeFavoritesView.kt) —
  the view contract the androidMain layer implements to render the store's state.
- [AnimeFavoritesController](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/AnimeFavoritesController.kt) —
  wires the store to its view and to `AnimeDatabaseStore`.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-favorites"))`
- `AnimeFavoritesMainStore`'s binding lives in this module's commonMain kotlin-inject-anvil
  component (`AnimeFavoritesComponent`, contributed to `FeatureScope`) and is merged by the
  same-named `@ContributesSubcomponent(FeatureScope::class)` in `androidMain`.
  `AnimeFavoritesFragment` doesn't build that subgraph itself; it reads its dependencies off
  `AnimeFavoritesScreenComponent` (`androidMain`), which wraps a Decompose `ComponentContext`
  around an already-built `AnimeFavoritesComponent`. The Activity hosting the fragment must
  implement `AnimeFavoritesScreenComponentHolder` (`androidMain`), exposing the currently active
  `AnimeFavoritesScreenComponent`. `AnimeFavoritesView` has no DI wiring; the `androidMain` layer
  (`AnimeFavoritesViewImpl`) implements it directly. `AnimeFavoritesController` has no DI wiring
  either; `AnimeFavoritesFragment` constructs it directly from `AnimeFavoritesScreenComponent`'s
  store and lifecycle.

## How to use it

Implement `AnimeFavoritesView` (an `AnimeFavoritesViewImpl`): render `UiModel` in `render()` and
call `dispatch(Intent)` from the relevant UI callbacks (item clicks, episode-viewed buttons,
notification toggle). On the favorites screen, construct `AnimeFavoritesController` with the
store, `AnimeDatabaseStore`, and the screen's lifecycle, then call
`controller.onViewCreated(viewImpl, viewLifecycle)`.
