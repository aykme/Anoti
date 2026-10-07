# The iOS host

How the Swift app is wired to the shared code: the three Swift files, what they call in which order,
the representable that shows the Compose screen, and what `Info.plist` must hold. The Android side
and the full startup order of both platforms are in [platform-mirroring.md](platform-mirroring.md).

Read when: changing Swift app code, `Info.plist`, or the `iosMain` code Swift calls.

## Files

- The app struct is `@main`. It creates the app delegate through `@UIApplicationDelegateAdaptor`
  and shows one `WindowGroup` with the content view.
  Example: [AnotiApp.swift](../../iosApp/iosApp/AnotiApp.swift).
- The app delegate starts the shared app and answers which orientations a window may take.
  Example: [AppDelegate.swift](../../iosApp/iosApp/AppDelegate.swift).
- The content view holds the scene's `@SceneStorage` string, watches the scene phase and the
  window size, and wraps the shared view controller in a `UIViewControllerRepresentable`. A black
  background sits behind it, so nothing white shows before Compose draws.
  Example: [ContentView.swift](../../iosApp/iosApp/ContentView.swift).
- The privacy manifest `PrivacyInfo.xcprivacy` sits beside them; what it declares is in
  [security-and-privacy.md "iOS privacy manifest"](security-and-privacy.md#ios-privacy-manifest).
- Swift imports the one framework, `Shared`, and reaches a Kotlin `object` through `.shared`. The
  framework itself is in [kmp.md](kmp.md), section "The iOS framework".
- Swift never logs; see [logging.md](../rules/logging.md). Swift is linted as
  [swiftlint.md](../rules/ios/swiftlint.md) says. Adding or renaming a Swift file changes the
  generated Xcode project; see [xcode-project.md](../rules/ios/xcode-project.md).

## Entry point

- Swift reaches the shared code only through `IosApp` in the `iosMain` of `core-kmp:di-app`. Keep
  the iOS `DiAppComponent` `internal`. A public one would put itself and every one of its
  supertypes into the framework's header.
  Example: [IosApp](../../core-kmp/di-app/src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/IosApp.kt).

## Call order

- Call `IosApp.start()` inside `application(_:didFinishLaunchingWithOptions:)`, reached through
  `@UIApplicationDelegateAdaptor`. iOS wants the background task registered and the notification
  delegate set before launch ends.
- Call `IosApp.viewController(restoredState:)` from `makeUIViewController` only, never from
  `updateUIViewController`. Pass it the scene's `@SceneStorage` string; an empty one means nothing
  was kept.
- Call `IosApp.saveState()` whenever the scene phase leaves `.active`. Store a returned string in
  the same `@SceneStorage`. On `nil`, leave the stored value as it is.
- A Swift-only event Kotlin must know of, a log line included, becomes one more `IosApp` function
  Swift calls with plain values. Swift itself never logs; see [logging.md](../rules/logging.md).

What the string holds, and when a kept one is dropped, is in
[state-restoration.md](state-restoration.md), section "iOS".

## The representable

- Create the representable once per scene and keep its identity: no `.id`, no condition around it.
  A second representable shows a second screen over the same root, and both bind to it.
- The view controller's representable must ignore every safe area, the keyboard's included. The
  Compose content pads itself for the system bars and the keyboard.

How the Compose content pads itself is in
[accessibility-and-adaptive-layout.md](accessibility-and-adaptive-layout.md).

## Property list

- The app must have one scene: `UIApplicationSupportsMultipleScenes` is `false`.
- `Info.plist` must list the background-task identifier the iOS background scheduler registers
  (Example: [AnimeBackgroundSchedulerImpl](../../feature-kmp/anime-background-update/src/iosMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/ios/impl/domain/scheduler/AnimeBackgroundSchedulerImpl.kt)),
  and `UIBackgroundModes` must hold `fetch`. Without that mode iOS refuses every refresh request.
  It must also set `CADisableMinimumFrameDurationOnPhone` to `true`, or Compose stops the app at
  launch.
- The list of identifiers is `BGTaskSchedulerPermittedIdentifiers`.
  Example: [Info.plist](../../iosApp/iosApp/Info.plist).

## Orientation and status bar

- The app turns only on a wide window, whatever the device. A window whose smaller side is under
  600 points keeps it upright. Android makes the same choice from the whole display at its stock
  density. So `Info.plist` must allow every orientation on the iPhone too, and the app must decide
  from the window's size at run time.
- The status bar must show light content over the app's dark screens.
- The app delegate answers `application(_:supportedInterfaceOrientationsFor:)` with
  `IosApp.supportedInterfaceOrientations(window:)`, converted from the `UInt64` Kotlin hands over.
  It is safe before `IosApp.start()`.
- When the window's size changes, the root view controller is told to check its orientations
  again, so a window crossing 600 points turns or locks at once.

The light status bar comes from `Info.plist`: `UIViewControllerBasedStatusBarAppearance` is
`false`, and `UIStatusBarStyle` is `UIStatusBarStyleLightContent`. A `GeometryReader` behind the
screen observes the size change. It calls `setNeedsUpdateOfSupportedInterfaceOrientations()` on
each scene's root view controller.
Example: [ContentView.swift](../../iosApp/iosApp/ContentView.swift).

## Release and Debug

- Release behaves as Debug in the code, on Swift and Kotlin alike. The rule that holds this is
  [swift-entry.md](../rules/ios/swift-entry.md).
