The iOS app: a thin SwiftUI host that shows the shared Compose screen and hands every decision to
`IosApp` in the `Shared` framework.

## Entities

- [iOSApp](iosApp/iOSApp.swift) — the app, one window over the shared screen.
- [AppDelegate](iosApp/AppDelegate.swift) — starts the shared app and answers how its windows
  may turn.
- [ContentView](iosApp/ContentView.swift) — the window's content, with the screen's state kept in
  the scene's storage.

## How to include it

Nothing depends on it. Xcode builds it from [iosApp.xcodeproj](iosApp.xcodeproj), generated from
[project.yml](project.yml), and its build phase compiles the `Shared` framework from
[`core-kmp:di-app`](../core-kmp/di-app/CORE-KMP-DI-APP-README.md) with Gradle first. The UI tests
live in `iosAppUITests/`.
