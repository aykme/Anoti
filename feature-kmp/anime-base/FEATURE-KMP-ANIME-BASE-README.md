Shikimori API access shared by the anime feature modules.

## Entities

- [ShikimoriApiService](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/service/ShikimoriApiService.kt) —
  Shikimori endpoints for the anime list and a single anime's details.
- [AnimeShortResponse](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/response/AnimeShortResponse.kt) —
  one anime list item.
- [AnimeDetailsResponse](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/response/AnimeDetailsResponse.kt) —
  full details for one anime.
- [AnimeResponseToListItemDomainMapper](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/data/mapper/AnimeResponseToListItemDomainMapper.kt) —
  maps response fields (image, release status) to domain-ready values.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-base"))`
- `ShikimoriApiService` is provided via `DiAnimeBaseComponent`, mixed into `DiAppComponent` on
  both platforms — inject it, don't construct it yourself.

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
