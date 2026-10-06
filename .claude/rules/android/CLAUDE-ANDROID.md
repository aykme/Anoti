# CLAUDE-ANDROID.md

The index of the Android rules. It is part of [CLAUDE.md](../../../CLAUDE.md), which holds the
rules both platforms share, and loads with it.

- Every check that needs the app running belongs on an emulator. A physical device attached for
  development is the developer's own and is not a test bench. An emulator also allows what a phone
  refuses: `adb root`, forcing an orientation, and picking the API level a branch needs.

## Index

- [tests.md](tests.md) — read before writing an `androidHostTest`, a composable test or an
  instrumented test.
- [instrumented-tests.md](instrumented-tests.md) — read before running every test of a task, or
  adding instrumented tests to a module.
- [r8-minified.md](r8-minified.md) — read before building or installing the minified variant, and
  when a change touches reflection, `Class.forName`, kotlinx.serialization, Room entities or DAOs,
  WorkManager workers, or a class the manifest names.
- [no-logic-proof.md](no-logic-proof.md) — read when finishing a task that changes no logic.
- [ci.md](ci.md) — read before starting or reading an `android.yml` run.
