# Anoti — full regression

The app's whole manual test plan. It holds no steps of its own: every check lives in the
regression file of the module that owns the behavior, and this file is the index of those.

A full regression means working through **every** file linked below, start to finish, and
running each one the way its own opening section says, through every pass listed under
"Passes" below. A module is only done when its file is done.

Before starting, read every file linked below and build one plan for the whole run. The same
action turns up in several files. Opening the app is the first step of nearly all of them. Put
each repeated action in the plan once, and check everything that depends on it in one go.
Nothing is done a second time just because a second file asks for it too.

To regress a single module instead, go straight to that module's own file; this index is for
the whole app.

A full regression is run for one platform at a time. A run on Android works through the
Android app and the shared modules. A run on iOS works through the iOS app and the shared
modules.

## Passes

A file that draws something on screen is run four times, once per combination, unless its
opening section says otherwise:

| Pass | System font size and display size | Orientation |
|------|-----------------------------------|-------------|
| 1    | default                           | portrait    |
| 2    | default                           | landscape   |
| 3    | both at maximum                   | portrait    |
| 4    | both at maximum                   | landscape   |

Pass 1 is the one that must be perfect. Passes 2–4 look for the same failures every time: text
cut off or overlapping, a control pushed off-screen or shrunk until it cannot be tapped, a row
that wraps in one pass and clips in another, and anything that stops responding to a tap because
it moved. Where a step behaves differently by scale or orientation on purpose, that step says so.

A phone keeps the app upright, so on a phone passes 2 and 4 repeat passes 1 and 3. The landscape
passes need a wide screen: a tablet, or a foldable unfolded.

Unless a step says otherwise, start from a fresh installation with the device online.

## Which build to run on

Run a regression on the build users get, never on a debug one: `minified` on Android, Release
on iOS. Their behavior can differ from debug. Android's is shrunk and obfuscated by R8; iOS's is
optimized and stripped, the way an archived build is. No automated test runs on them, so this
regression is the check that sees what users see.

## The build to run on, on Android

`./gradlew :androidApp:assembleMinified` produces a `minified` build that also installs. The
release build carries no signing config of its own, so it cannot go on a device.

Shrinking removes and renames code. It is the step that can break something which only shows
once the app is running. A check on a build that skipped it proves nothing about what ships.

## The build to run on, on iOS

Release runs in a Simulator or on an iPhone. It is built with the Kotlin framework's release
link. In Xcode, pick it under Product, Scheme, Edit Scheme, Run, Build Configuration. An iPhone
also needs a signing team: put yours into `TEAM_ID` in `iosApp/Configuration/Config.xcconfig`,
and do not commit it.

## Platforms

Every file under "Shared modules" describes what the app does, not what one platform does. The
files under "Android app" and "iOS app" cover what only that platform's shell does. Where a step
in a shared file goes through a system screen, it names Android's. A file whose behavior differs
on iOS says so in a Platforms section of its own. A check only one platform can reach names that
platform and says why.

## Android app

- [androidApp](androidApp/ANDROIDAPP-REGRESS.md)

## iOS app

- [iosApp](iosApp/IOSAPP-REGRESS.md)

## Shared modules

### Entry module

- [main](main/MAIN-REGRESS.md)

### Core modules

- [core-kmp:anime-database](core-kmp/anime-database/CORE-KMP-ANIME-DATABASE-REGRESS.md)
- [core-kmp:celebrity](core-kmp/celebrity/CORE-KMP-CELEBRITY-REGRESS.md)
- [core-kmp:di-app](core-kmp/di-app/CORE-KMP-DI-APP-REGRESS.md)
- [core-kmp:di-scope](core-kmp/di-scope/CORE-KMP-DI-SCOPE-REGRESS.md)
- [core-kmp:navigation](core-kmp/navigation/CORE-KMP-NAVIGATION-REGRESS.md)
- [core-kmp:network](core-kmp/network/CORE-KMP-NETWORK-REGRESS.md)
- [core-kmp:test-utils](core-kmp/test-utils/CORE-KMP-TEST-UTILS-REGRESS.md)

### Feature modules

- [feature-kmp:anime-background-update](feature-kmp/anime-background-update/FEATURE-KMP-ANIME-BACKGROUND-UPDATE-REGRESS.md)
- [feature-kmp:anime-base](feature-kmp/anime-base/FEATURE-KMP-ANIME-BASE-REGRESS.md)
- [feature-kmp:anime-favorites](feature-kmp/anime-favorites/FEATURE-KMP-ANIME-FAVORITES-REGRESS.md)
- [feature-kmp:anime-list](feature-kmp/anime-list/FEATURE-KMP-ANIME-LIST-REGRESS.md)
- [feature-kmp:anime-notification](feature-kmp/anime-notification/FEATURE-KMP-ANIME-NOTIFICATION-REGRESS.md)
- [feature-kmp:anime-notification-external](feature-kmp/anime-notification-external/FEATURE-KMP-ANIME-NOTIFICATION-EXTERNAL-REGRESS.md)
- [feature-kmp:bottom-navigation-bar](feature-kmp/bottom-navigation-bar/FEATURE-KMP-BOTTOM-NAVIGATION-BAR-REGRESS.md)
- [feature-kmp:notifications-rationale-dialog](feature-kmp/notifications-rationale-dialog/FEATURE-KMP-NOTIFICATIONS-RATIONALE-DIALOG-REGRESS.md)
