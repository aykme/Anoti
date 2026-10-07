# Versioning and release

How the app's version numbers are set for both platforms, which build types and configurations
exist and what each is for, and the signing state. Release builds are guarded by two rules this
file links rather than retells.

Read when: bumping the app version or a platform version; choosing a build type or configuration
for a check; asking how a shipped build is signed.

## Version scheme

Both platforms take the app version from one pair of entries under the KMP header of
[gradle/libs.versions.toml](../../gradle/libs.versions.toml).

- `versionName` grows in steps of 0.1: `1.0`, `1.1`, `1.2`. iOS reads each dot-separated part as a
  whole number and has no hundredths, so `1.09` is not a valid step.
- `versionCode` is `versionName` without the dot: `1.1` is `11`.
- Android reads both in the Android host's `defaultConfig`. Example:
  [androidApp/build.gradle.kts](../../androidApp/build.gradle.kts).
- iOS gets `versionName` as `MARKETING_VERSION` and `versionCode` as `CURRENT_PROJECT_VERSION`,
  through a generated xcconfig. Where iOS versions live, and how that file is regenerated and
  committed, is in [ios/versions.md](../rules/ios/versions.md).

A version bump changes both entries together and nothing else by hand.

## Platform versions

- The Android SDK levels (`compileSdk`, `minSdk`, `targetSdk`) and `jvmTarget` sit under the
  Android header of the catalog.
- The iOS deployment target, the Xcode release and the XcodeGen release sit under the iOS header.
- A catalog comment next to `minSdk`, `iosDeploymentTarget`, `xcode` and `agp` names what else
  changes with it, such as a line of the root `README.md`. The root `README.md` itself follows the
  rule in [CLAUDE.md](../../CLAUDE.md#git-and-github).
- How the catalog is organized is in [.claude/design/tech-stack.md](tech-stack.md).

## Build types and configurations

| Platform | Build | What it is for | Owner |
|---|---|---|---|
| Android | `debug` | Development and every automated test | [tests.md](../rules/tests.md) |
| Android | `minified` | What ships, installable: release plus the debug key | [android/r8-minified.md](../rules/android/r8-minified.md) |
| Android | `release` | Shrunk and obfuscated; carries no signing config | [android/r8-minified.md](../rules/android/r8-minified.md) |
| iOS | Debug | Development, simulator tests and UI tests | [ios/ci.md](../rules/ios/ci.md) |
| iOS | Release | Stripped as an archive is, with a dSYM | [ios/release-build.md](../rules/ios/release-build.md) |

- The two rules in the Owner column hold the settings that must not change and the checks each
  build gets. Read them before touching a build type or a configuration.
- No automated test runs on `minified`, `release` or Release:
  [tests.md "Which build"](../rules/tests.md#which-build). Where a regression is run from is in
  [.claude/design/documentation.md](documentation.md).
- How the iOS code keeps Release from behaving differently from Debug is in
  [.claude/design/ios-host.md](ios-host.md).

## Signing

- Android `release` has no signing config. `minified` signs with the debug key, so it installs on
  a device. Example: [androidApp/build.gradle.kts](../../androidApp/build.gradle.kts).
- iOS leaves `TEAM_ID` empty, so the project sets no signing team. Simulator builds need none, and
  CI builds with `CODE_SIGNING_ALLOWED=NO`. Example:
  [Config.xcconfig](../../iosApp/Configuration/Config.xcconfig).
- No step in the build or in CI uploads a build to a store.
