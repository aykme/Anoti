Fetches fresh data for the user's saved anime library in the background and notifies about
newly aired episodes.

## Entities

- [AnimeUpdateManager](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/manager/AnimeUpdateManager.kt) —
  runs the anime-library update and returns its outcome.
- [UpdateAllAnimeInBackgroundOnceUsecase](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/usecase/UpdateAllAnimeInBackgroundOnceUsecase.kt) —
  triggers a one-off background update.
- [AnimeBackgroundUpdateSource](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/source/AnimeBackgroundUpdateSource.kt) —
  fetches fresh anime data from the Shikimori API.
- [AnimeBackgroundScheduler](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/scheduler/AnimeBackgroundScheduler.kt) —
  schedules periodic background updates on the host platform.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-background-update"))`
- `AnimeUpdateManager`, `UpdateAllAnimeInBackgroundOnceUsecase` and `AnimeBackgroundScheduler`
  are all provided via this module's per-platform `DiAnimeBackgroundUpdatePlatformComponent`
  (a separate one on Android and on iOS), mixed into `DiAppComponent` on both platforms — inject
  them, don't construct them yourself.
- On Android the `Application` must implement WorkManager's `Configuration.Provider` and serve
  `DiAppComponent.workManagerConfiguration`. The manifest disables WorkManager's default
  initializer, so WorkManager initializes itself from that provider on first access.
- The iOS `AnimeBackgroundScheduler` registers its `BGAppRefreshTask` handler as soon as it's
  created — see its own KDoc for a documented Info.plist registration gap.

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
