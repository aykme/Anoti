# CLAUDE-IOS.md

The rules for the iOS side of the project. They are part of [CLAUDE.md](CLAUDE.md), which holds
the rules both platforms share and loads this file.

## Building for iOS on this machine

- Kotlin/Native compiles the iOS targets on this Windows machine. Nothing is linked here and
  nothing runs here.
- Every link task reports `SKIPPED`, and the build still ends with `BUILD SUCCESSFUL`. Read the
  task's own line, never the last one.
- The local iOS check is these five tasks, run from the root:

  ```
  ./gradlew compileKotlinIosArm64 compileKotlinIosSimulatorArm64 \
    compileTestKotlinIosSimulatorArm64 compileCommonMainKotlinMetadata \
    compileIosMainKotlinMetadata
  ```

- `compileCommonMainKotlinMetadata` compiles the shared code as metadata. It catches an error
  the IDE shows in `commonMain` and no target compile does.
- Run the check on every task that touches `commonMain`, `iosMain` or a build file.
- `DiAppComponent` in the `iosMain` of `core-kmp:di-app` is the iOS dependency graph. Its
  compiling is the proof that every iOS binding wires together.

## The framework

- One iOS framework exists, `Shared`. `:core-kmp:di-app` links it, since it sits above every
  other module.
- No other module declares a framework. A second one would bring a second copy of the Kotlin
  runtime and of the shared state.
- `core-kmp:di-app` applies the Compose plugins for the framework's sake. Compose copies the
  resources of the modules below into an app bundle through the module that links it.
- The Compose compiler is switched on for the native targets only in that module. On Android it
  would change the DI classes, and with them what R8 produces. Keep the restriction.

## What Swift calls

- Swift reaches the shared code only through `IosApp` in the `iosMain` of `core-kmp:di-app`.
  Keep the iOS `DiAppComponent` `internal`. A public one would put itself and every one of its
  supertypes into the framework's header.
- Call `IosApp.start()` inside `application(_:didFinishLaunchingWithOptions:)`, reached through
  `@UIApplicationDelegateAdaptor`. iOS wants the background task registered and the
  notification delegate set before launch ends.
- Call `IosApp.viewController()` from `makeUIViewController` only, never from
  `updateUIViewController`. Each call builds a new screen.
- Create the representable once per scene and keep its identity: no `.id`, no condition around
  it. A representable made again while the app is in front starts the screen afresh.
- The view controller's representable must ignore every safe area, the keyboard's included. The
  Compose content pads itself for the system bars and the keyboard.
- The app must have one scene: `UIApplicationSupportsMultipleScenes` is `false`.
- `Info.plist` must list the background-task identifier the iOS `AnimeBackgroundSchedulerImpl`
  registers, and `UIBackgroundModes` must hold `fetch`. Without that mode iOS refuses every
  refresh request. It must also set `CADisableMinimumFrameDurationOnPhone` to `true`, or Compose
  stops the app at launch.
- The app turns only on a wide window, whatever the device, as on Android. A window whose smaller
  side is under 600 points keeps it upright, as Android keeps a screen below sw600dp. So
  `Info.plist` must allow every orientation on the iPhone too, and the app must decide from the
  window's size at run time.
- The status bar must show light content over the app's dark screens.

## Versions

- Every iOS version lives in `gradle/libs.versions.toml`, under the iOS headers.
- `iosApp/Configuration/Version.xcconfig` is generated from the catalog by
  `./gradlew generateIosVersionXcconfig`. Every build of `androidApp` runs that task too.
- Never edit the file by hand. Commit a version change together with the regenerated file.
