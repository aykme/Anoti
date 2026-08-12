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
- `AnimeUpdateManager` is provided via Dagger on Android (`app`'s `AppModule`, until Phase 9 of
  the DI migration) and via `AnimeUpdateManagerIosComponent`, contributed to `AppScope`'s
  merged component, on iOS — inject it, don't construct it yourself.
- `UpdateAllAnimeInBackgroundOnceUsecase` is provided via Dagger setup in `androidMain`
  (`AnimeOnceBackgroundUpdateModule`, backed by `WorkManager`) — inject it too. No iOS
  implementation exists yet.
- `AnimeBackgroundScheduler` is iOS-only for now (`AnimeBackgroundSchedulerPlatformComponent`,
  `AppScope`) — it registers its `BGAppRefreshTask` handler as soon as it's created; see the
  iOS implementation's own KDoc for a documented Info.plist registration gap. Android's
  periodic scheduling still goes through `WorkManager` directly
  (`AnimePeriodicBackgroundUpdateModule`), with no equivalent commonMain abstraction yet.

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
