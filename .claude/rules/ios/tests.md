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
- Before writing or changing an `iosTest`, a test for `iosMain` code, or `iosMain` code, read
  [.claude/design/testing-platforms.md](../../design/testing-platforms.md), section "iOS". It holds
  what belongs in `iosTest` and what goes in `commonMain` instead.
- The tests of the iOS-only code (see [.claude/design/kmp.md](../../design/kmp.md), section "Where
  code lives") are first written where they run on any machine: `commonTest`, or `androidHostTest`
  for a composable. They move with the code once they pass.
