# Security and privacy

How the app treats input from outside, what it asks permission for, what it sends and keeps, and
what it declares to the stores. The app talks to one public API without accounts or keys, and
it keeps its persisted data on the device.

Read when: reading input from outside the app (an intent extra, a notification payload, a
network response), adding a permission, a network host or a persisted field, writing a log line
with data in it, or changing a manifest, the R8 rules or the iOS privacy manifest.

## Untrusted input

- Inside the app, code fails fast; input from outside never makes it throw. The principle is in
  [principles.md](principles.md). Here, outside input is parsed defensively, and anything
  malformed becomes "nothing".
- The Android activity is exported, as the launcher needs. Any app can start it with any extra.
  Reading the extra sits inside `runCatching`, since unpacking a foreign extra can itself throw.
  Example:
  [MainActivity.kt](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt).
- A deep-link payload decodes to a root config or to `null`; nothing it holds may throw. Both
  platforms read a notification tap through the same decoder. Example:
  [NavRootDeepLink.kt](../../core-kmp/navigation/src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootDeepLink.kt).
- A `null` target opens no particular screen. How a tap navigates is in
  [navigation.md](navigation.md), section "Deep links".
- The notification's `PendingIntent` is `FLAG_IMMUTABLE`, so no other app can change what it
  opens. Example:
  [AnimeNotificationIntentProviderImpl.kt](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/provider/AnimeNotificationIntentProviderImpl.kt).
- A network response is decoded with unknown keys ignored, and every failure becomes a result
  value instead of an exception. Both are in [data-layer.md](data-layer.md), section "Network".
- Text the user types is capped in length before it reaches the store. Example:
  [AnimeListTopBar.kt](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListTopBar.kt).

## Network

- The app calls one host over HTTPS. Its base URL is a constant of the network core module, and
  image paths from responses are joined to it. Example:
  [NetworkConsts.kt](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/NetworkConsts.kt).
- Neither platform allows cleartext. Android has no network security config and no
  `usesCleartextTraffic`; iOS has no App Transport Security exception in its `Info.plist`.
- The API needs no account and no key, so the repository holds no secret. `local.properties`
  is ignored by git, and the iOS signing team stays empty in the committed xcconfig. See
  [iosApp/Configuration/Config.xcconfig](../../iosApp/Configuration/Config.xcconfig).

## Permissions

- A module's manifest declares the permissions its own code needs. App-wide ones live in the
  Android host's manifest. The manifest convention is in
  [module-anatomy.md](module-anatomy.md), section "Manifests and resources".
- Today's set:
  - `POST_NOTIFICATIONS`, declared by the notification module that posts them;
  - `INTERNET`, declared by the Android host;
  - `com.google.android.gms.permission.AD_ID`, declared by the Android host for Google Play's
    review. The `play-services-appset` dependency is kept for it; the reason sits beside it in
    [androidApp/build.gradle.kts](../../androidApp/build.gradle.kts).
- iOS asks for alert and sound authorization only. Background refresh is declared in
  `Info.plist`; see [ios-host.md](ios-host.md).

## Asking for a permission

- The root host decides what to do from the platform's report. When notifications are allowed,
  nothing happens. When the system can ask and wants no explanation, its own question shows
  right away.
- When the system wants an explanation first, the app's rationale dialog shows. Its approval
  brings the system's question.
- When the system can no longer ask, the dialog shows too, and its approval opens the app's
  notification settings. Example:
  [NotificationPermissionAction.kt](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/permission/NotificationPermissionAction.kt).
- The platform reports the status and runs the requests. The decision itself is shared code.

## Data at rest

- The app keeps one Room database: on Android in its private database directory, on iOS in its
  documents directory. It holds the data the features persist.
- Nothing is encrypted at rest; nothing the app keeps is a secret.
- Android backup is on (`android:allowBackup="true"`) with no backup rules, so the system's auto
  backup includes the database. See
  [androidApp/src/main/AndroidManifest.xml](../../androidApp/src/main/AndroidManifest.xml).

## Logs and builds

- What a log line may carry, and never carries, is in [logging.md](../rules/logging.md).
- The release build is shrunk and obfuscated, and its stack traces name no source file. How, and
  what must survive, is in [r8-minified.md](../rules/android/r8-minified.md).

## iOS privacy manifest

- [iosApp/iosApp/PrivacyInfo.xcprivacy](../../iosApp/iosApp/PrivacyInfo.xcprivacy) declares no
  tracking, no tracking domains and no collected data.
- It lists two required-reason API categories with their reason codes: file timestamps and
  system boot time.
- When the app or a library starts using another required-reason API, its entry is added there.
- `Info.plist` declares that the app uses no non-exempt encryption.
