---
paths:
  - "**/iosApp/iosApp/**"
  - "**/src/iosMain/**"
---

# What Swift calls, and how the iOS app is wired

- Swift reaches the shared code only through `IosApp` in the `iosMain` of `core-kmp:di-app`. Keep
  the iOS `DiAppComponent` `internal`. A public one would put itself and every one of its
  supertypes into the framework's header.
- Call `IosApp.start()` inside `application(_:didFinishLaunchingWithOptions:)`, reached through
  `@UIApplicationDelegateAdaptor`. iOS wants the background task registered and the notification
  delegate set before launch ends.
- Call `IosApp.viewController(restoredState:)` from `makeUIViewController` only, never from
  `updateUIViewController`. Pass it the scene's `@SceneStorage` string; an empty one means nothing
  was kept.
- Call `IosApp.saveState()` whenever the scene phase leaves `.active`. Store a returned string in
  the same `@SceneStorage`. On `nil`, leave the stored value as it is.
- Create the representable once per scene and keep its identity: no `.id`, no condition around it.
  A second representable shows a second screen over the same root, and both bind to it.
- The view controller's representable must ignore every safe area, the keyboard's included. The
  Compose content pads itself for the system bars and the keyboard.
- The app must have one scene: `UIApplicationSupportsMultipleScenes` is `false`.
- `Info.plist` must list the background-task identifier the iOS `AnimeBackgroundSchedulerImpl`
  registers, and `UIBackgroundModes` must hold `fetch`. Without that mode iOS refuses every refresh
  request. It must also set `CADisableMinimumFrameDurationOnPhone` to `true`, or Compose stops the
  app at launch.
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
- Release must not behave differently from Debug in the code: no `#if DEBUG`, `assert` or
  `isDebugBinary` decides what the app does. No automated test runs on Release; see
  [release-build.md](release-build.md).
- Adding or renaming a Swift file changes the generated Xcode project; see
  [xcode-project.md](xcode-project.md).
