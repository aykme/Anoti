Shikimori API access shared by the anime feature modules.

## Entities

- [ShikimoriApiService](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/service/ShikimoriApiService.kt) —
  pages of anime from Shikimori GraphQL, by filter or by id.
- [AnimeListResponseToAnimeResponsesMapper](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/mapper/AnimeListResponseToAnimeResponsesMapper.kt) —
  unwraps a GraphQL answer into its anime, or fails.
- [AnimeResponseToListItemDomainMapper](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/mapper/AnimeResponseToListItemDomainMapper.kt) —
  maps response fields (id, poster, release status) to domain-ready values.
- [PullToRefreshBox](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/presentation/compose/PullToRefreshBox.kt) —
  a full-size swipe-to-refresh surface, shared by every screen state that should let the user
  pull down to retry.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-base"))`
- `ShikimoriApiService` is provided via `DiAnimeBaseComponent`, mixed into `DiAppComponent` on
  both platforms — inject it, don't construct it yourself.

## How to use it

```kotlin
suspend fun getOngoingPage(page: Int): CallResult<List<ListItemDomain>> = safeApi.call {
    service.getAnimeList(
        page = page,
        releaseStatus = ReleaseStatusData.ONGOING.value,
        sort = SortData.SCORE.value,
        search = null,
        ids = null
    ).toAnimeResponses().mapNotNull { it.toListItemDomain() }
}
```
