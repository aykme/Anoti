# Anoti — full regression

The app's whole manual test plan. It holds no steps of its own: every check lives in the
regression file of the module that owns the behavior, and this file is the index of those.

A full regression means working through **every** file linked below, start to finish, and
running each one the way its own opening section says. Including its pass matrix, where the
same script is repeated at the default and the maximum text and display size, in both
orientations. A module is only done when its file is done.

Before starting, read every file linked below and build one plan for the whole run. The same
action turns up in several files. Opening the app is the first step of nearly all of them. Put
each repeated action in the plan once, and check everything that depends on it in one go.
Nothing is done a second time just because a second file asks for it too.

To regress a single module instead, go straight to that module's own file; this index is for
the whole app.

## The build to run on

Run a regression on a build that has been shrunk and obfuscated, the way the released one is.
`./gradlew :app:assembleMinified` produces one that also installs. The release build carries no
signing config of its own, so it cannot go on a device.

Shrinking removes and renames code. It is the step that can break something which only shows
once the app is running. A check on a build that skipped it proves nothing about what ships.

## Platforms

Every file below describes what the app does, not what one platform does. Where a step goes
through a system screen, it names Android's. A file whose behavior differs on iOS says so in a
Platforms section of its own. A check only one platform can reach names that platform and says
why. Running any of this on iOS needs an iOS build of the app to install first.

## App modules

- [app](app/APP-REGRESS.md)
- [main](main/MAIN-REGRESS.md)

## Core modules

- [core-kmp:anime-database](core-kmp/anime-database/CORE-KMP-ANIME-DATABASE-REGRESS.md)
- [core-kmp:celebrity](core-kmp/celebrity/CORE-KMP-CELEBRITY-REGRESS.md)
- [core-kmp:di-app](core-kmp/di-app/CORE-KMP-DI-APP-REGRESS.md)
- [core-kmp:di-scope](core-kmp/di-scope/CORE-KMP-DI-SCOPE-REGRESS.md)
- [core-kmp:navigation](core-kmp/navigation/CORE-KMP-NAVIGATION-REGRESS.md)
- [core-kmp:network](core-kmp/network/CORE-KMP-NETWORK-REGRESS.md)
- [core-kmp:test-utils](core-kmp/test-utils/CORE-KMP-TEST-UTILS-REGRESS.md)

## Feature modules

- [feature-kmp:anime-background-update](feature-kmp/anime-background-update/FEATURE-KMP-ANIME-BACKGROUND-UPDATE-REGRESS.md)
- [feature-kmp:anime-base](feature-kmp/anime-base/FEATURE-KMP-ANIME-BASE-REGRESS.md)
- [feature-kmp:anime-favorites](feature-kmp/anime-favorites/FEATURE-KMP-ANIME-FAVORITES-REGRESS.md)
- [feature-kmp:anime-list](feature-kmp/anime-list/FEATURE-KMP-ANIME-LIST-REGRESS.md)
- [feature-kmp:anime-notification](feature-kmp/anime-notification/FEATURE-KMP-ANIME-NOTIFICATION-REGRESS.md)
- [feature-kmp:anime-notification-external](feature-kmp/anime-notification-external/FEATURE-KMP-ANIME-NOTIFICATION-EXTERNAL-REGRESS.md)
- [feature-kmp:bottom-navigation-bar](feature-kmp/bottom-navigation-bar/FEATURE-KMP-BOTTOM-NAVIGATION-BAR-REGRESS.md)
- [feature-kmp:notifications-rationale-dialog](feature-kmp/notifications-rationale-dialog/FEATURE-KMP-NOTIFICATIONS-RATIONALE-DIALOG-REGRESS.md)

