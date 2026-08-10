Provides access to the "main" module from the "anime-notification" module without direct coupling.

## Entities

- [AnimeNotificationIntentProvider](src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/android/impl/presentation/provider/AnimeNotificationIntentProvider.kt) —
  supplies the deep-link intent for navigating to the anime favorites screen from notifications.

## How to include it

- Gradle: `api(project(":feature-kmp:anime-notification-external"))`
- `AnimeNotificationIntentProvider` is a contract with no implementation in this module — a
  consuming app must implement it and bind it into Dagger itself. The current binding lives in
  `:main` (`AnimeNotificationIntentProviderImpl`, bound by `AnimeNotificationIntentProviderModule`
  and included in `:app`'s `AppModule`); inject the interface, don't construct it yourself.

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
