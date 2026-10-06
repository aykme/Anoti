---
paths:
  - "**/.claude/rules/ios/ci.md"
  - "**/.github/workflows/ios.yml"
  - "**/.github/scripts/ios-*"
  - "**/.github/scripts/test/**"
---

# Running iOS on CI

When it runs, and how to start and follow a run, is in [../ci-github.md](../ci-github.md). When a
task must offer a run is in [../finishing-a-task.md](../finishing-a-task.md).

- `ios.yml` runs on GitHub's macOS runner. On any other branch than `develop` it runs only on the
  developer's word, started by them or by Claude. A push links the framework, runs every
  Kotlin/Native test, builds the app in Debug and runs SwiftLint with its analyzer. It installs no
  app.
- A run by hand picks the rest through its inputs. The `plan` job turns them into the flags the
  other jobs read, and gives a push their defaults. `.github/scripts/ios-ci-plan.sh` does it; after
  a change to it, run `bash .github/scripts/test/ios-ci-plan-test.sh`.
  - `unit_tests`, on by default: the Kotlin/Native tests.
  - `ui_flow`, off: `AnimeFavoritesUserFlowTest`.
  - `ios_tests`, off: `OrientationUITests`, and the restore and theme checks.
  - `swiftlint`, on: SwiftLint and its analyzer.
  - `device`, `phone`: where the app's tests run, `phone`, `tablet` or `all`. The restore checks
    need the iPhone.
  - `orientation`, `portrait`: the orientations `AnimeFavoritesUserFlowTest` walks. The iPhone never
    turns, so it runs the flow only in portrait. A run that leaves the flow no device fails.
  - `setup_check`: proves `compile-kotlin-framework.sh` finds a Java; see
    [xcode-project.md](xcode-project.md).
- Its `swiftlint` job lints the Swift sources on Linux, in a minute or two. Its `app` job runs
  SwiftLint's analyzer rules over the Debug build log. Both follow `swiftlint`.
- Started with `-f lint_only=true`, it runs the `swiftlint` job and nothing else besides `plan`,
  whatever `swiftlint` says. Such a run carries no media.
- With `device` set to `tablet`, the app's tests run on an iPad instead of the iPhone, and with
  `all` on both. The iPad walks the flow once per orientation. That happens only when the developer
  asks for it. The 600-point rule itself is unit-tested whenever `unit_tests` is on, but the app
  turning on a real wide window is tested only on the iPad.
- Started with `-f release=true`, it builds the app in Release and runs no automated test. The
  release check ends it: the executable is stripped, and its dSYM matches and decodes Kotlin. Such a
  run is due after a change to the Release settings, the check or its job. `release` overrides
  every other input.
- After a run that started the app, its `ios-media` artifact goes to the developer's folder
  `iOS test` on the current user's desktop: `~/Desktop/iOS test/<date>_<run id>_<short commit>/`.
  When the iPad ran, through `device`, `ios-media-ipad` goes into an `ipad` subfolder there.
  Nothing there is deleted.
- After a Release run, its `ios-release` artifact goes to
  `~/Desktop/iOS test/<date>_<run id>_<short commit>_release/`. Its `ios-dsym` stays on GitHub for
  seven days.
- A failed UI test, and a failed restore case, gets up to three tries; see
  [../tests-on-device.md](../tests-on-device.md). Each case starts from a fresh installation of its
  own, so the cases run alone and in any order. Every failed try stays in the log, and a restore
  case keeps each try's media in a folder of its own.
- The restore checks restart the simulator before their first case. After a UI test has launched
  the app, iOS hands no kept scene state back to the app until the simulator restarts.
