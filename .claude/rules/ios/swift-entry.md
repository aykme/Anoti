---
paths:
  - "**/iosApp/iosApp/**"
  - "**/src/iosMain/**"
---

# What Swift calls, and how the iOS app is wired

- Before changing Swift app code, `Info.plist`, the `iosMain` code Swift calls or the iOS
  `DiAppComponent`, read [.claude/design/ios-host.md](../../design/ios-host.md). It holds how
  Swift reaches the shared code, the order of the calls, the representable, what `Info.plist`
  must set, orientation and the status bar.
- Release must not behave differently from Debug in the code: no `#if DEBUG`, `assert` or
  `isDebugBinary` decides what the app does. No automated test runs on Release; see
  [release-build.md](release-build.md).
- Adding or renaming a Swift file changes the generated Xcode project; see
  [xcode-project.md](xcode-project.md).
