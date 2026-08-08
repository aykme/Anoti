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
  the view contract the platform layer implements to render the main store's state.
- [AnimeListController](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/AnimeListController.kt) —
  wires all four stores and `AnimeDatabaseStore` to the view.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-list"))`
- All four stores are provided via the platform module's Dagger setup
  (`feature-platform/anime-list`) — inject them, don't construct them yourself. `AnimeListView`
  has no DI wiring; the consumer implements it directly (see `feature-platform/anime-list`'s
  `AnimeListViewImpl`). `AnimeListController` has no DI wiring either; construct it directly with
  the stores and lifecycle.

## How to use it

Implement `AnimeListView` (an `AnimeListViewImpl`): render `UiModel` in `render()` and call
`dispatch(Intent)` from the relevant UI callbacks (tab clicks, search text changes, pagination).
On the anime list screen, construct `AnimeListController` with the main store, the three section
stores, `AnimeDatabaseStore`, and the screen's lifecycle, then call
`controller.onViewCreated(viewImpl, viewLifecycle)`.
