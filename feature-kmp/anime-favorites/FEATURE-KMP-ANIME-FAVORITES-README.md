The favorites screen: an MVI store over the user's saved anime, with episode-viewed tracking
and per-item notification toggles.

## Entities

- [AnimeFavoritesMainStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/store/AnimeFavoritesMainStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.
- [AnimeFavoritesView](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/AnimeFavoritesView.kt) —
  the view contract `AnimeFavoritesRoute` implements to render the store's state.
- [AnimeFavoritesController](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/AnimeFavoritesController.kt) —
  wires the store to its view and to `AnimeDatabaseStore`.
- [AnimeFavoritesRoute](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/AnimeFavoritesRoute.kt) —
  renders the screen for a given `NavAnimeFavoritesScreenComponent`, wiring the view and
  controller internally.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-favorites"))`
- `AnimeFavoritesMainStore`'s binding lives in this module's commonMain
  `DiAnimeFavoritesComponent` (a `FeatureScope` kotlin-inject `@Component`, taking
  `DiAnimeFavoritesDependencies` as its constructor parent). `AnimeFavoritesRoute` doesn't build
  that component itself; it takes an already-built `NavAnimeFavoritesScreenComponent`
  (`commonMain`), which wraps a Decompose `ComponentContext` around a
  `DiAnimeFavoritesComponent` — the caller builds that instance and passes it in.
  `AnimeFavoritesView` and `AnimeFavoritesController` have no DI wiring; `AnimeFavoritesRoute`
  constructs and binds them internally.

## How to use it

```kotlin
// Called once per NavRootConfig.AnimeFavorites activation, with the screen component that
// activation built:
AnimeFavoritesRoute(screenComponent = navAnimeFavoritesScreenComponent, topInsetDp = topInsetDp)
```
