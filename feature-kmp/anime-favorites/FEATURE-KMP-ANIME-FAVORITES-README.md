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
- `AnimeFavoritesMainStore`'s binding lives in this module's commonMain
  `DiAnimeFavoritesComponent` (a `FeatureScope` kotlin-inject `@Component`, taking
  `DiAnimeFavoritesDependencies` as its constructor parent). `AnimeFavoritesFragment` doesn't
  build that component itself; it reads its dependencies off `NavAnimeFavoritesScreenComponent`
  (`commonMain`), which wraps a Decompose `ComponentContext` around an already-built
  `DiAnimeFavoritesComponent`. The Activity hosting the fragment must implement
  `NavAnimeFavoritesScreenComponentHolder` (`androidMain`), exposing the currently active
  `NavAnimeFavoritesScreenComponent`. `AnimeFavoritesView` has no DI wiring; the `androidMain`
  layer (`AnimeFavoritesFragment`, via an anonymous `ComposeMviView` from `core-kmp:celebrity`)
  implements it directly. `AnimeFavoritesController` has no DI wiring either;
  `AnimeFavoritesFragment` constructs it directly from `NavAnimeFavoritesScreenComponent`'s store
  and lifecycle.

## How to use it

`AnimeFavoritesView` is backed by a `ComposeMviView` rendering `UiModel` into this module's own
Compose UI; dispatch `Intent`s from the relevant UI callbacks (item clicks, episode-viewed
buttons, notification toggle). On the favorites screen, construct `AnimeFavoritesController` with
the store, `AnimeDatabaseStore`, and the screen's lifecycle, then call
`controller.onViewCreated(view, viewLifecycle)`.
