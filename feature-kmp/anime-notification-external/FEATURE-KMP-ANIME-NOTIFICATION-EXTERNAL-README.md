Lets the "anime-notification" module open the app's screens from a notification tap without
depending on the "main" module that owns them.

## Entities

- [AnimeNotificationTapPayloadProvider](src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/kmp/api/presentation/provider/AnimeNotificationTapPayloadProvider.kt) —
  builds what a notification carries for its tap.

Android notifications open the app through an intent instead, supplied by the module's Android
contract, `AnimeNotificationIntentProvider`.

## How to include it

- Gradle: `api(project(":feature-kmp:anime-notification-external"))`
- Both are contracts. This module holds no implementation of them besides a test double for the
  Android one. A consuming app implements them and contributes the bindings itself.
- The implementations live in `:main`. `AnimeNotificationIntentProviderImpl` is bound by
  `DiRootPlatformComponent` in the Android `DiAppComponent`.
  `AnimeNotificationTapPayloadProviderImpl` is bound by `DiRootNotificationTapComponent` in the
  iOS one. Inject the interfaces, don't construct them yourself.

## How to use it

The Android notification manager builds the intent once, when it is created, and sets it as the
content intent of every new-episode notification. The group summary carries none. The iOS
manager builds the payload for each notification and puts it in the notification's `userInfo`.
The app reads it back when the notification is tapped.
