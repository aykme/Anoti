Lets the "anime-notification" module open the app's screens from a notification tap without
depending on the "main" module that owns them.

## Entities

- [AnimeNotificationTapPayloadProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/kmp/api/presentation/provider/AnimeNotificationTapPayloadProvider.kt) —
  builds what a notification carries for its tap.
- [AnimeNotificationIntentProvider](src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/android/impl/presentation/provider/AnimeNotificationIntentProvider.kt) —
  supplies the deep-link intent for navigating to the anime favorites screen from notifications.

## How to include it

- Gradle: `api(project(":feature-kmp:anime-notification-external"))`
- Both are contracts with no implementation in this module besides a test double. A consuming
  app implements them and contributes the bindings itself. The implementations live in `:main`:
  `AnimeNotificationIntentProviderImpl`, bound by `DiRootPlatformComponent` in the Android
  `DiAppComponent`, and `AnimeNotificationTapPayloadProviderImpl`, bound by
  `DiRootNotificationTapComponent` in the iOS one. Inject the interfaces, don't construct them
  yourself.

## How to use it

The notification manager reads its platform's provider once per notification. On Android the
intent becomes the notification's content intent. On iOS the payload becomes the notification's
`userInfo`, and the app reads it back when the notification is tapped.
