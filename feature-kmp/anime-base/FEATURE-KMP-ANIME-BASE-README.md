Shikimori API access shared by the anime feature modules.

## Entities

- [ShikimoriApiService](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/service/ShikimoriApiService.kt) —
  Shikimori endpoints for the anime list, a single anime's details and the posters of a page.
- [AnimePosterSource](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/domain/source/AnimePosterSource.kt) —
  best-effort real posters by anime id.
- [AnimeShortResponse](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/response/AnimeShortResponse.kt) —
  one anime list item.
- [AnimeDetailsResponse](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/response/AnimeDetailsResponse.kt) —
  full details for one anime.
- [AnimeResponseToListItemDomainMapper](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/mapper/AnimeResponseToListItemDomainMapper.kt) —
  maps response fields (image or poster, release status) to domain-ready values.
- [PullToRefreshBox](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/presentation/compose/PullToRefreshBox.kt) —
  a full-size swipe-to-refresh surface, shared by every screen state that should let the user
  pull down to retry.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-base"))`
- `ShikimoriApiService` and `AnimePosterSource` are provided via `DiAnimeBaseComponent`, mixed
  into `DiAppComponent` on both platforms — inject them, don't construct them yourself.

## How to use it

```kotlin
class AnimeSource(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) {
    suspend fun ongoingAnime(page: Int): CallResult<List<AnimeShortResponse>> = safeApi.call {
        service.getAnimeList(
            page = page,
            releaseStatus = ReleaseStatusData.ONGOING.value,
            sort = SortData.POPULARITY.value,
            search = null,
            ids = null
        )
    }
}
```
