Fetches fresh data for the user's saved anime library in the background and notifies about
newly aired episodes.

## Entities

- [AnimeUpdateManager](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/manager/AnimeUpdateManager.kt) —
  runs the anime-library update and returns its outcome.
- [UpdateAllAnimeInBackgroundOnceUsecase](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/usecase/UpdateAllAnimeInBackgroundOnceUsecase.kt) —
  triggers a one-off background update.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-background-update"))`
- `AnimeUpdateManager` is provided via the `app` module's Dagger setup (it's consumed by
  `feature-platform/anime-background-update`'s `AnimeUpdateWorker`) — inject it, don't construct
  it yourself.
- `UpdateAllAnimeInBackgroundOnceUsecase` is provided via the platform module's Dagger setup
  (`feature-platform/anime-background-update`, backed by `WorkManager`) — inject it too.

## How to use it

```kotlin
// Android example (no iOS example yet) — run one update pass, called from a WorkManager
// CoroutineWorker:
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
