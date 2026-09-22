The anime list screen: ongoing/announced/search sections, each with its own paginated MVI store,
coordinated by a top-level store.

## Entities

- [DiAnimeListDependencies](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/di/DiAnimeListDependencies.kt) —
  what the screen's component takes from its parent.
- [NavAnimeListScreenComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponent.kt) —
  owns the screen's dependency graph and its saved state.
- [AnimeListRoute](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/AnimeListRoute.kt) —
  renders the screen for a given screen component.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-list"))`
- Satisfy [DiAnimeListDependencies](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/di/DiAnimeListDependencies.kt)
  from the app's root component, then build a `DiAnimeListComponent` with
  `createDiAnimeListComponent(parent)` and wrap it in a `NavAnimeListScreenComponent` together
  with the Decompose `ComponentContext` the screen's navigation child owns. The stores, the
  view and the controller have no wiring of their own to do — `AnimeListRoute` builds and binds
  them from that component.

## How to use it

```kotlin
// Called once per NavRootConfig.AnimeList activation, with the screen component that
// activation built:
AnimeListRoute(screenComponent = navAnimeListScreenComponent)
```
