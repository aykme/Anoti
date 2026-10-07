# Platform mirroring

How Android and iOS stay twins: what a platform counterpart looks like, the order each platform
starts in, the table of counterparts by role, and the differences accepted on purpose. The Swift
side of iOS is in [ios-host.md](ios-host.md).

Read when: adding or changing code that one platform has and the other must mirror; changing the
`Application`, the screen host, the iOS entry object or anything they start; writing a `*Worker`,
a background refresh, a notification or a permission request.

## Twins

- What one platform does, the other does in the matching place, under the matching name. The
  principle is in [principles.md](principles.md).
- A platform counterpart keeps the simple name and changes only the platform segment of its
  package; [module-anatomy.md](module-anatomy.md), section "Naming", owns that. The app graph twins
  even share one package.
  Example: [iOS DiAppComponent](../../core-kmp/di-app/src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt).
- Where the counterpart has another name, the KDoc names it. The iOS entry object names the
  Android `Application`, and the iOS screen host names `MainActivity`.
  Example: [IosScreenHost](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/api/presentation/IosScreenHost.kt).
- What both twins compute is written once in `commonMain` and called by both; see
  [kmp.md](kmp.md), section "Where code lives". The root host is that shape: both screen hosts
  build the same `RootHost`.
  Example: [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt).
- Platform components and their packages are in
  [dependency-injection.md](dependency-injection.md), section "Module and platform components".

## Startup

Android, as found in
[AnotiApp](../../androidApp/src/main/kotlin/com/alekseivinogradov/anoti/impl/presentation/AnotiApp.kt)
and [MainActivity](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt):

1. `Application.onCreate` builds the app graph with `DiAppComponent::class.create`, before
   `super.onCreate()`.
2. It launches its start-up work on the app's main context, and that work moves to the I/O
   dispatcher. It creates the notification channel first, then schedules the periodic work. The
   worker posts into the channel, so the channel exists before any work is enqueued.
3. WorkManager starts on demand. The host's manifest removes its startup initializer, and the
   `Application` serves `Configuration.Provider` from the app graph.
4. The system creates the screen host. `super.onCreate()` runs first, so the saved state is
   readable.
5. The screen host takes a new root graph and the process's permission session from the
   `Application` through `DiRootComponentHolder`. On a fresh start only, it reads the tap target.
6. It builds the root host, listens for new intents, sets the system bars and the orientation,
   and sets the root content.
7. Last, it reads the notification permission status and hands it to the root host.

iOS, as found in
[IosApp](../../core-kmp/di-app/src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/IosApp.kt)
and [IosScreenHost](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/api/presentation/IosScreenHost.kt):

1. SwiftUI creates the app delegate. Its `didFinishLaunching` calls the entry object's `start()`.
   A second call does nothing.
2. `start()` builds the app graph through its `internal` creator. It reads the scheduler, and
   building it registers the background-task handler. Then it schedules the periodic refresh.
3. It builds the screen host over the app graph and starts it. Starting sets the notification
   center's delegate, so a tap that launched the app is delivered.
4. The scene shows the content view. Its representable's `makeUIViewController` asks the entry
   object for a view controller, passing the scene's kept string.
5. The first request builds the root: the root graph, the root host, and the saved state from that
   string. Later requests return the same root.
6. On the root's first resume, the notification permission status is read and handed to the root
   host.

## Mirror table

| Role | Android | iOS |
|---|---|---|
| Process entry | `Application` subclass in the Android host, named by its manifest | `IosApp` object in the composition root's `iosMain`, called from Swift |
| App graph | `DiAppComponent` in `androidMain`, built with `::class.create` | `DiAppComponent` in `iosMain`, `internal`, built with `createDiAppComponent` |
| Screen host | `MainActivity` in the entry module's `androidMain` | `IosScreenHost` in the entry module's `iosMain` |
| Root | a new `RootHost` each time the activity is created | one `RootHost` for the process, held by `IosRootHolder` |
| Root graph | from the `Application` through `DiRootComponentHolder` | from a function the screen host hands to `IosRootHolder` |
| Saved state | Decompose's `defaultComponentContext()` over the saved instance state | one JSON string in the scene's `@SceneStorage` |
| Saved-state version drop | none in the app's code | a state written by another app version is dropped |
| Tap delivery | a `PendingIntent` to the screen host; the extra is read on a fresh start, `addOnNewIntentListener` while it lives | the notification center's delegate; a target that arrives before the root is held for it |
| Tap contract | an external contract building the `PendingIntent` | an external contract building the payload map |
| Posting | `NotificationManagerCompat` into a channel created at start-up | `UNUserNotificationCenter`; no channel |
| Background refresh | WorkManager unique periodic work; workers built by a factory from the app graph | a `BGTaskScheduler` app refresh task, registered at launch, resubmitted as each run starts |
| One-off refresh | WorkManager unique one-time work | a single-flight usecase running in its own scope |
| Notification permission | `POST_NOTIFICATIONS` through an activity result launcher | `requestAuthorization` on the notification center |
| Orientation | `requestedOrientation` from the whole display at stock density | `supportedInterfaceOrientations` from the window's size in points |
| Keyboard and insets | `adjustResize` and edge-to-edge | `ignoresSafeArea` and `OnFocusBehavior.DoNothing` |
| HTTP engine | OkHttp | Darwin |
| Status bar | `enableEdgeToEdge` with dark bar styles | `UIStatusBarStyleLightContent` in `Info.plist` |

Where the details live:

- Saved state, the restore order and the version drop: [state-restoration.md](state-restoration.md).
- Tap targets and the live root: [navigation.md](navigation.md), section "Deep links".
- Orientation threshold, insets and keyboard:
  [accessibility-and-adaptive-layout.md](accessibility-and-adaptive-layout.md).
- The HTTP client: [data-layer.md](data-layer.md), section "Network".
- Both permission flows go through the common `NotificationPermissionRequests` and the root host.
  Example: [IosNotificationPermissionRequests](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/permission/IosNotificationPermissionRequests.kt).
- Both orientation answers come from one common threshold.
  Example: [WindowRotation.kt](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/orientation/WindowRotation.kt).
- The posting twins share their text and ids from `commonMain`.
  Example: [iOS AnimeNotificationManagerImpl](../../feature-kmp/anime-notification/src/iosMain/kotlin/com/alekseivinogradov/anoti/animenotification/ios/impl/presentation/manager/AnimeNotificationManagerImpl.kt).
- The background refresh twins.
  Example: [Android AnimeBackgroundSchedulerImpl](../../feature-kmp/anime-background-update/src/androidMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/android/impl/domain/scheduler/AnimeBackgroundSchedulerImpl.kt),
  [iOS AnimeBackgroundSchedulerImpl](../../feature-kmp/anime-background-update/src/iosMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/ios/impl/domain/scheduler/AnimeBackgroundSchedulerImpl.kt).

## Accepted asymmetries

- **Root lifetime.** Android builds a root, with its root graph, each time the activity is
  created. iOS builds one root per process, and the scene's saved state is read into it once. The
  decision is in [decisions.md](decisions.md).
- **Tap contracts.** A notification carries its tap as a `PendingIntent` on Android and as data on
  iOS. So the entry module contributes `DiRootPlatformComponent` from `androidMain` to the Android
  app graph. It contributes `DiRootNotificationTapComponent` from `commonMain`, and only the iOS app
  graph mixes it in.
  Example: [DiRootNotificationTapComponent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/di/DiRootNotificationTapComponent.kt).
- **Permission session.** Both platforms keep one per process. Android keeps it in the
  `Application`, so it outlives a recreated activity. iOS keeps it in the root holder, which lives
  as long as the process.
- **Notification channel.** Only Android has one, created at start-up. iOS posts without it.
- **Platform context.** It is the application `Context` on Android. On iOS it carries nothing,
  and the app graph takes it only so both twins are built the same way.
  Example: [IosAppContext](../../core-kmp/di-app/src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/IosAppContext.kt).
- **Deferred graph access.** Both platforms pass a function instead of a value into the
  scheduler, for different reasons; see [dependency-injection.md](dependency-injection.md),
  section "Bindings".
- **iOS update passes.** They run in a scope the platform component builds, with its own job and
  handler; see
  [concurrency-and-lifecycle.md "Coroutine contexts"](concurrency-and-lifecycle.md#coroutine-contexts).
- **Code only iOS needs.** iOS lacks the saved instance state Android keeps, so the entry module's
  `iosMain` holds the code that keeps and rebuilds the root. Its placement is in [kmp.md](kmp.md),
  section "Where code lives".
