Fetches fresh data for the user's saved anime library in the background and notifies about
newly aired episodes.

## Entities

- [AnimeBackgroundScheduler](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/scheduler/AnimeBackgroundScheduler.kt) —
  schedules the repeating update on the host platform.
- [UpdateAllAnimeInBackgroundOnceUsecase](src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/api/domain/usecase/UpdateAllAnimeInBackgroundOnceUsecase.kt) —
  triggers a one-off background update.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-background-update"))`
- Both are provided through this module's per-platform `DiAnimeBackgroundUpdatePlatformComponent`
  (a separate one on Android and on iOS), mixed into `DiAppComponent` — inject them, don't
  construct them yourself.
- On Android the `Application` must implement WorkManager's `Configuration.Provider` and serve
  `DiAppComponent.workManagerConfiguration`. The manifest disables WorkManager's default
  initializer, so WorkManager initializes itself from that provider on first access.
- On iOS the scheduler registers its `BGAppRefreshTask` handler as soon as it is created, and
  the task identifier has to be listed in the app target's Info.plist — see its own KDoc.
