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
- The four stores' bindings live in this module's commonMain `DiAnimeListComponent` (a
  `FeatureScope` kotlin-inject `@Component`, taking `DiAnimeListDependencies` as its constructor
  parent). `AnimeListFragment` doesn't build that component itself; it reads its dependencies off
  `NavAnimeListScreenComponent` (`commonMain`), which wraps a Decompose `ComponentContext`
  around an already-built `DiAnimeListComponent`. The Activity hosting the fragment must implement
  `NavAnimeListScreenComponentHolder` (`androidMain`), exposing the currently active
  `NavAnimeListScreenComponent`. `AnimeListView` has no DI wiring; the `androidMain` layer
  (`AnimeListFragment`, via an anonymous `ComposeMviView` from `core-kmp:celebrity`) implements
  it directly. `AnimeListController` has no DI wiring either; `AnimeListFragment` constructs it
  directly from `NavAnimeListScreenComponent`'s stores and lifecycle.

## How to use it

`AnimeListView` is backed by a `ComposeMviView` rendering `UiModel` into this module's own
Compose UI; dispatch `Intent`s from the relevant UI callbacks (tab clicks, search text changes,
pagination). On the screen hosting it, construct `AnimeListController` with the main store, the
three section stores, `AnimeDatabaseStore`, and the screen's lifecycle, then call
`controller.onViewCreated(view, viewLifecycle)`.
