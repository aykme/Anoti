Shows the "new episode aired" notification for anime in the user's favorites.

## Entities

- [AnimeNotificationManager](src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/kmp/api/domain/manager/AnimeNotificationManager.kt) —
  shows the "new episode aired" notification.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-notification"))`
- `AnimeNotificationManager` is provided via this module's per-platform
  `DiAnimeNotificationPlatformComponent`, mixed into `DiAppComponent` on both Android and iOS —
  inject it, don't construct it yourself. Android's binding also needs an
  [AnimeNotificationIntentProvider](../anime-notification-external/FEATURE-KMP-ANIME-NOTIFICATION-EXTERNAL-README.md)
  implementation contributed by the consuming app.

## How to use it

```kotlin
class AnimeUpdateManagerImpl(
    private val notificationManager: AnimeNotificationManager,
    // ...
) {
    private fun onEpisodeAired(anime: AnimeDbDomain) {
        notificationManager.makeNewEpisodeNotification(
            animeName = anime.name,
            airedEpisode = anime.episodesAired,
            imageUrl = anime.imageUrl
        )
    }
}
```
