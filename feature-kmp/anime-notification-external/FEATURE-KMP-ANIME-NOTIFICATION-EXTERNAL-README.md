Provides access to the "main" module from the "anime-notification" module without direct coupling.

## Entities

- [AnimeNotificationIntentProvider](src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/android/impl/presentation/provider/AnimeNotificationIntentProvider.kt) —
  supplies the deep-link intent for navigating to the anime favorites screen from notifications.

## How to include it

- Gradle: `api(project(":feature-kmp:anime-notification-external"))`
- `AnimeNotificationIntentProvider` is provided via Dagger (the "anime-notification" module
  depends on it) — inject it into your code, don't construct it yourself.

## How to use it

```kotlin
// Android example (no iOS example yet):
class AnimeNotificationManagerImpl(
    private val animeNotificationIntentProvider: AnimeNotificationIntentProvider
) : AnimeNotificationManager {
    private val intent = animeNotificationIntentProvider.getNewEpisodeNotificationIntent(appContext)
    // Use intent in notification builder
}
```
