The favorites screen: an MVI store over the user's saved anime, with episode-viewed tracking
and per-item notification toggles.

## Entities

- [DiAnimeFavoritesDependencies](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/di/DiAnimeFavoritesDependencies.kt) —
  what the screen's component takes from its parent.
- [NavAnimeFavoritesScreenComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/NavAnimeFavoritesScreenComponent.kt) —
  owns the screen's dependency graph and its saved state.
- [AnimeFavoritesRoute](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/AnimeFavoritesRoute.kt) —
  renders the screen for a given screen component.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-favorites"))`
- Satisfy [DiAnimeFavoritesDependencies](src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/di/DiAnimeFavoritesDependencies.kt)
  from the app's root component, then build a `DiAnimeFavoritesComponent` with
  `createDiAnimeFavoritesComponent(parent)` and wrap it in a `NavAnimeFavoritesScreenComponent`
  together with the Decompose `ComponentContext` the screen's navigation child owns. The store,
  the view and the controller have no wiring of their own to do — `AnimeFavoritesRoute` builds
  and binds them from that component.

## How to use it

```kotlin
// Called once per NavRootConfig.AnimeFavorites activation, with the screen component that
// activation built:
AnimeFavoritesRoute(screenComponent = navAnimeFavoritesScreenComponent)
```
