Shows the "new episode aired" notification for anime in the user's favorites.

## Entities

- [AnimeNotificationManager](src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/kmp/api/domain/manager/AnimeNotificationManager.kt) —
  shows the "new episode aired" notification.

## How to include it

- Gradle: `implementation(project(":feature-kmp:anime-notification"))`
- `AnimeNotificationManager` is provided via the platform module's Dagger setup
  (`feature-platform/anime-notification`) — inject it, don't construct it yourself.

## How to use it

```kotlin
// Android example (no iOS example yet):
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
