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
  the view contract the androidMain layer implements to render the main store's state.
- [AnimeListController](src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/AnimeListController.kt) —
  wires all four stores and `AnimeDatabaseStore` to the view.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-list"))`
- The four stores' bindings live in this module's commonMain kotlin-inject-anvil component
  (`AnimeListComponent`, contributed to `FeatureScope`) and are merged by the same-named
  `@ContributesSubcomponent(FeatureScope::class)` in `androidMain`, which `AnimeListFragment`
  builds per instance from its host Activity — so just use `AnimeListFragment`. The Activity
  hosting it must implement `AnimeListComponentFactoryHolder` (`androidMain`).
  `AnimeListView` has no DI wiring; the `androidMain` layer (`AnimeListViewImpl`) implements it
  directly. `AnimeListController` has no DI wiring either; construct it directly with the stores
  and lifecycle.

## How to use it

Use `AnimeListFragment` as your screen's fragment; the fragment wires the stores, view, and
controller internally. Alternatively, implement `AnimeListView` (as `AnimeListViewImpl` does):
render `UiModel` in `render()` and call `dispatch(Intent)` from the relevant UI callbacks
(tab clicks, search text changes, pagination). On the screen hosting it, construct
`AnimeListController` with the main store, the three section stores, `AnimeDatabaseStore`,
and the screen's lifecycle, then call `controller.onViewCreated(viewImpl, viewLifecycle)`.
