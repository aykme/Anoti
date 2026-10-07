# CLAUDE-IOS.md

The index of the iOS rules. It is part of [CLAUDE.md](../../../CLAUDE.md), which indexes the
rules both platforms share, and loads with it.

## Index

- [build-check.md](build-check.md) — read on every task that touches `commonMain`, `iosMain` or a
  build file, to run the local iOS check.
- [swift-entry.md](swift-entry.md) — read before changing Swift app code, `Info.plist`, or the
  `iosMain` code Swift calls; it sends you to the design's iOS host file.
- [xcode-project.md](xcode-project.md) — read before changing `project.yml`, the Xcode project, or
  `compile-kotlin-framework.sh`, and before adding or renaming a Swift file.
- [framework.md](framework.md) — read before changing `core-kmp:di-app` or a module's framework or
  Compose-compiler setup; it sends you to the design's KMP file.
- [versions.md](versions.md) — read before changing an iOS version.
- [release-build.md](release-build.md) — read before changing the Release settings or the release
  check.
- [swiftlint.md](swiftlint.md) — read before committing Swift or a change to how SwiftLint runs.
- [tests.md](tests.md) — read before writing an `iosTest` or tests for `iosMain` code; it sends
  you to the design's testing-platforms file.
- [ci.md](ci.md) — read before starting or reading an `ios.yml` run.
