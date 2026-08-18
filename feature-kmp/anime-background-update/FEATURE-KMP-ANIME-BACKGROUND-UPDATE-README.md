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
- Both `AnimeBackgroundScheduler` implementations do platform setup as soon as they're created:
  the Android one installs WorkManager's custom `Configuration`, the iOS one registers its
  `BGAppRefreshTask` handler — see the iOS implementation's own KDoc for a documented Info.plist
  registration gap.

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
