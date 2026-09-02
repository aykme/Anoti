The anime list screen: ongoing/announced/search sections, each with its own paginated MVI store,
coordinated by a top-level store.

## Entities

- [AnimeListMainStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/store/main/AnimeListMainStore.kt) —
  the top-level store. `State`/`Intent`/`Label` are documented on the type itself.
- [OngoingSectionStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/store/ongoingsection/OngoingSectionStore.kt) —
  the store for the "ongoing" section's list.
- [AnnouncedSectionStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/store/announcedsection/AnnouncedSectionStore.kt) —
  the store for the "announced" section's list.
- [SearchSectionStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/store/searchsection/SearchSectionStore.kt) —
  the store for the search section's list.
- [AnimeListView](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/presentation/AnimeListView.kt) —
  the view contract `AnimeListRoute` implements to render the main store's state.
- [AnimeListController](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/AnimeListController.kt) —
  wires all four stores and `AnimeDatabaseStore` to the view.
- [AnimeListRoute](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/AnimeListRoute.kt) —
  renders the screen for a given `NavAnimeListScreenComponent`, wiring the view and controller
  internally.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-list"))`
- The four stores' bindings live in this module's commonMain `DiAnimeListComponent` (a
  `FeatureScope` kotlin-inject `@Component`, taking `DiAnimeListDependencies` as its constructor
  parent). `AnimeListRoute` doesn't build that component itself; it takes an already-built
  `NavAnimeListScreenComponent` (`commonMain`), which wraps a Decompose `ComponentContext`
  around a `DiAnimeListComponent` — the caller builds that instance and passes it in.
  `AnimeListView` and `AnimeListController` have no DI wiring; `AnimeListRoute` constructs and
  binds them internally.

## How to use it

```kotlin
// Called once per NavRootConfig.AnimeList activation, with the screen component that
// activation built:
AnimeListRoute(screenComponent = navAnimeListScreenComponent)
```
