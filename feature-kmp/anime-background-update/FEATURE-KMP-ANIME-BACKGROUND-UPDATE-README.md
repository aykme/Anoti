Fetches fresh data for the user's saved anime library in the background and notifies about
newly aired episodes.

## Entities

- [AnimeUpdateManager](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/manager/AnimeUpdateManager.kt) —
  runs the anime-library update and returns its outcome.
- [UpdateAllAnimeInBackgroundOnceUsecase](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/usecase/UpdateAllAnimeInBackgroundOnceUsecase.kt) —
  triggers a one-off background update.
- [AnimeBackgroundUpdateSource](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/source/AnimeBackgroundUpdateSource.kt) —
  fetches fresh anime data from the Shikimori API.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-background-update"))`
- `AnimeUpdateManager` is provided via the `app` module's Dagger setup (the commonMain
  `AnimeUpdateManagerImpl` is wired by the `AnimePeriodicBackgroundUpdateModule` in
  `androidMain`) — inject it, don't construct it yourself.
- `UpdateAllAnimeInBackgroundOnceUsecase` is provided via Dagger setup in `androidMain`
  (`AnimeOnceBackgroundUpdateModule`, backed by `WorkManager`) — inject it too.

## How to use it

```kotlin
// Android example — run one update pass, called from a WorkManager CoroutineWorker:
class AnimeUpdateWorker(
    // ...
    private val animeUpdateManager: AnimeUpdateManager
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = when (animeUpdateManager.update()) {
        WorkResult.Success -> Result.success()
        WorkResult.Error -> Result.failure()
    }
}

// Trigger a one-off update outside the periodic schedule (e.g. a pull-to-refresh):
updateAllAnimeInBackgroundOnceUsecase.execute()
```
