---
paths:
  - "**/src/iosTest/**"
  - "**/src/iosMain/**"
---

# Tests for iOS code

- An `iosTest` runs on macOS and nowhere else: off macOS, as on the developer's Windows machine,
  `iosSimulatorArm64Test` is skipped outright, so a test there is compiled and never executed.
  Kover cannot measure Kotlin/Native either. Such a test proves nothing off macOS and counts for
  nothing. It does run on GitHub's macOS runner, in `ios.yml`, started on the developer's word.
- So before writing one, check whether the code under it needs an iOS API at all. A class built
  from coroutines, atomics and the module's own types belongs in `commonMain`, where its test runs
  on every build and is measured, even when iOS is its only caller. `BackgroundRefreshPass` and
  `BackgroundRefreshTask` in `feature-kmp:anime-background-update` show the shape: the portable
  half of a background refresh is common and tested, and `AnimeBackgroundSchedulerImpl` is left
  holding the `BGTaskScheduler` calls and nothing else.
- The iOS-only code of [../source-sets.md](../source-sets.md) is tested in `iosTest`, and stays
  outside the coverage number. Its tests are first written where they run on any machine:
  `commonTest`, or `androidHostTest` for a composable. They move with the code once they pass.
- What stays in `iosTest` is what only a real iOS runtime can answer. It is written knowing it runs
  only in `ios.yml`, never off macOS.
- An `iosTest` builds no UIKit window or view. The test process has no app, so UIKit starts only
  halfway, and the Compose tests in the same process then crash on text input.
