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
- Call `IosApp.viewController(restoredState:)` from `makeUIViewController` only, never from
  `updateUIViewController`. Pass it the scene's `@SceneStorage` string; an empty one means
  nothing was kept.
- Call `IosApp.saveState()` whenever the scene phase leaves `.active`. Store a returned string
  in the same `@SceneStorage`. On `nil`, leave the stored value as it is.
- Create the representable once per scene and keep its identity: no `.id`, no condition around
  it. A second representable shows a second screen over the same root, and both bind to it.
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
- The app delegate answers `application(_:supportedInterfaceOrientationsFor:)` with
  `IosApp.supportedInterfaceOrientations(window:)`, converted from the `UInt64` Kotlin hands over.
  It is safe before `IosApp.start()`.
- When the window's size changes, the root view controller is told to check its orientations
  again, so a window crossing 600 points turns or locks at once.

## Versions

- Every iOS version lives in `gradle/libs.versions.toml`, under the iOS headers.
- `iosApp/Configuration/Version.xcconfig` is generated from the catalog by
  `./gradlew generateIosVersionXcconfig`. Every build of `androidApp` runs that task too.
- Never edit the file by hand. Commit a version change together with the regenerated file.

## The Xcode project

- `iosApp/project.yml` is the source of the Xcode project. XcodeGen generates
  `iosApp/iosApp.xcodeproj` from it, and the generated project is committed, so the app opens
  in Xcode with nothing else to run.
- Never edit the project in Xcode or by hand. Change `project.yml`, and commit it together with
  the project the macOS runner generated from it: `ios.yml` uploads it as the `xcodeproj`
  artifact.
- The `drift` job of `ios.yml` fails when the committed project differs from what
  `project.yml` generates.
- The Xcode build phase runs `iosApp/scripts/compile-kotlin-framework.sh`. It finds a Java on its
  own, since Xcode starts it without the user's shell environment. The iOS build needs no Android
  SDK.

## Before committing Swift

SwiftLint checks every Swift file being committed, on the machine itself. It runs every lint rule
of `config/swiftlint/swiftlint.yml`. Its analyzer rules need a Mac, so they run in `ios.yml` only.

A commit makes a lint run due when it adds or changes a `*.swift` file. A change to how SwiftLint
runs does too: `config/swiftlint/swiftlint.yml`, `iosApp/scripts/swiftlint.sh`, or a `swiftLint*`
entry of the catalog. Nothing else does. SwiftLint reads no Kotlin, `iosMain` included. It reads
none of `Info.plist`, `project.yml`, the `.xcconfig` files and the asset catalogs either.

1. Run `bash iosApp/scripts/swiftlint.sh` with the committed Swift files as arguments, from
   `git diff --cached --name-only --diff-filter=d -- '*.swift'`. After a change to how SwiftLint
   runs, pass no argument, so every file is linted.
2. On exit code `1`, fix the findings and run it again until it exits `0`.
   `swiftlint.sh fix` corrects what it can, and its edits are read before they are committed. A
   finding that would need a substantial change to the logic goes to the developer.
3. On `2`, fix the setup problem the message names.
4. On `3`, this machine cannot run SwiftLint. Tell the developer at once what is missing and how
   to install it. On Windows that is `winget install Swift.Toolchain`. On macOS it is Xcode, and
   on Linux the swift.org toolchain. Offer three ways on:
   - install it now;
   - push the branch and start a lint-only run, as "Running iOS on CI" says, then fix, commit
     and run again until it passes;
   - commit without the lint, leaving the check to the push to `develop`.

   A push needs the developer's word every time. When the developer has said earlier not to
   stop, restate this rule and ask explicitly before any push.

The compiler covers two checks SwiftLint cannot make. The build treats Swift warnings as errors,
so a deprecated API stops it. `SWIFT_STRICT_CONCURRENCY` is `complete`, so a call into
`XCUIApplication` or `XCUIElement` off the main actor stops it too.

What is left to a person:

- The formatting of the files around it, where no rule speaks.
- A UI test's body is split by `//Given`, `//When` and `//Then`, as in Kotlin.
- Imports nothing uses. The analyzer finds them on CI only.
- Read the CI build log of the round for warnings, since nothing here compiles Swift.

## Running iOS on CI

- `ios.yml` runs on GitHub's macOS runner on every push to `develop`. On any other branch it
  runs only on the developer's word, by them or by Claude. It links the framework, runs every
  Kotlin/Native test, builds the app in Debug and Release, runs the UI tests on an iPhone and on
  an iPad, and walks the restore and theme checks.
- Its `swiftlint` job lints the Swift sources on Linux, in a minute or two. Its `app` job runs
  SwiftLint's analyzer rules over the Debug build log.
- Started with `-f lint_only=true`, it runs the `swiftlint` job and nothing else. Such a run
  carries no media. A change to the analyzer rules needs a full run, since `lint_only` skips `app`.
- A task branch that touches `iosMain`, `iosApp/`, `config/swiftlint/` or a build file ends by
  asking the developer whether to run it on that branch.
- After a full run, its `ios-media` and `ios-media-ipad` artifacts go to the developer's folder
  `iOS test` on the current user's desktop, `*\Desktop\iOS test\<date>_<run id>_<short commit>\`,
  the iPad one in an `ipad` subfolder. Nothing there is deleted.
- The UI tests and the restore checks run the real app, which reaches the live backend. The
  developer allowed them, as they allowed Android's `AnimeFavoritesUserFlowTest`. Their
  assertions stay on structure.
- A failed UI test, and a failed restore case, gets up to three tries. Each case starts from a
  fresh installation of its own, so the cases run alone and in any order. Every failed try stays
  in the log, and a restore case keeps each try's media in a folder of its own.
- The restore checks restart the simulator before their first case. After a UI test has
  launched the app, iOS hands no kept scene state back to the app until the simulator restarts.
- Start it by hand as "CI on GitHub" in `CLAUDE.md` says. Add `-f setup_check=true` after a
  change to `compile-kotlin-framework.sh`: that job proves the script finds a Java on a Mac
  without one in `JAVA_HOME`.
- After a change to `project.yml`, take the project the run generated with
  `gh run download <run id> -n xcodeproj -D iosApp/iosApp.xcodeproj`. The artifact holds the
  folder's contents, so the folder is named in `-D`.
