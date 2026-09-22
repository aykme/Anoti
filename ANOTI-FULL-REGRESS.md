# Anoti — full regression

The app's whole manual test plan. It holds no steps of its own: every check lives in the
regression file of the module that owns the behavior, and this file is the index of those.

A full regression means working through **every** file linked below, start to finish, and
running each one the way its own opening section says — including its pass matrix, where the
same script is repeated at the default and the maximum text and display size, in both
orientations. A module is only done when its file is done.

To regress a single module instead, go straight to that module's own file; this index is for
the whole app.

## Core modules

- [core-kmp:anime-database](core-kmp/anime-database/CORE-KMP-ANIME-DATABASE-REGRESS.md)
- [core-kmp:celebrity](core-kmp/celebrity/CORE-KMP-CELEBRITY-REGRESS.md)
- [core-kmp:di-app](core-kmp/di-app/CORE-KMP-DI-APP-REGRESS.md)
- [core-kmp:di-scope](core-kmp/di-scope/CORE-KMP-DI-SCOPE-REGRESS.md)
- [core-kmp:navigation](core-kmp/navigation/CORE-KMP-NAVIGATION-REGRESS.md)
- [core-kmp:network](core-kmp/network/CORE-KMP-NETWORK-REGRESS.md)
- [core-kmp:test-utils](core-kmp/test-utils/CORE-KMP-TEST-UTILS-REGRESS.md)

## Feature modules

- [feature-kmp:anime-favorites](feature-kmp/anime-favorites/FEATURE-KMP-ANIME-FAVORITES-REGRESS.md)
- [feature-kmp:anime-list](feature-kmp/anime-list/FEATURE-KMP-ANIME-LIST-REGRESS.md)

## Modules with no regression file yet

These have no file, so a full regression run does not cover them. Anything they add to the app
is unchecked until each one gets its own file, at which point it is linked above instead.

- `feature-kmp:anime-background-update`
- `feature-kmp:anime-base`
- `feature-kmp:anime-notification`
- `feature-kmp:anime-notification-external`
- `feature-kmp:bottom-navigation-bar`
- `feature-kmp:notifications-rationale-dialog`
