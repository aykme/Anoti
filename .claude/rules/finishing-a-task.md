---
paths:
  - "**/.claude/rules/finishing-a-task.md"
---

# Finishing a task

Run this once, when the task is being finished (e.g. its final review or last step), not after
each subtask. The Android checks below apply to every task, not only to one that touched Android
code. The reviewer-prompt rule under "Review" applies to every review, mid-task ones included.

## Correctness

- Verify that the logic you wrote actually works. Don't assume it from reading the code.
- If the task was a refactor or a migration, ask the developer whether the resulting behavior must
  match the previous behavior exactly. If yes, verify there's no difference in the final result. A
  difference that turns out unavoidable is agreed with the developer before proceeding.

## Tests

- Make sure the tests cover every case that can realistically occur, concurrency included where
  needed. No duplicate tests, no excessive coverage that adds nothing.
- Run every test in each affected module, not only the ones this task wrote, and confirm all are
  green. Then take the test rules one by one against what the module now holds, and fix whatever
  doesn't conform: [tests.md](tests.md), [tests-on-device.md](tests-on-device.md),
  [android/tests.md](android/tests.md), [ios/tests.md](ios/tests.md), and the design files they
  send you to, [.claude/design/testing.md](../design/testing.md) and
  [.claude/design/testing-platforms.md](../design/testing-platforms.md).
- The Android instrumented tests are part of that run, on an emulator. Read
  [android/instrumented-tests.md](android/instrumented-tests.md) for the tasks and the clean
  installation.
- For every new or fixed test, confirm it doesn't flake, doesn't rely on real time, and never makes
  real API calls. Real time is acceptable only in exceptional cases agreed with the developer. The
  one way past the API rule is a test that launches the real app, with the developer's permission;
  see [tests-on-device.md](tests-on-device.md).
- Measure the coverage of every module you touched instead of estimating it from the diff, and
  name what is still uncovered. Read [test-coverage.md](test-coverage.md) for the commands.
- Then run `./gradlew koverVerify` and get it green, before the code review below. Nothing else in
  the build runs it, and it is the gate the whole project is held to. A red one is not reported as
  a finding; it is fixed.

## Builds and platforms

- If the task touched `commonMain`, `iosMain` or a build file, run the local iOS check in
  [ios/build-check.md](ios/build-check.md).
- Some code is reached by name: reflection, `Class.forName`, kotlinx.serialization, Room entities
  and DAOs, WorkManager workers, or a class the manifest names. If the change touches it, read
  [android/r8-minified.md](android/r8-minified.md) and check it still works once R8 has had it.
- A task that changes no logic skips every on-device check: the instrumented tests, the minified
  walk and the manual regression. A rename, a file move, and an edit to comments or documentation
  are such tasks.
- Such a task proves itself by comparing a build made before the change with one made after it:
  [android/no-logic-proof.md](android/no-logic-proof.md). A difference beyond what that file allows
  means the change was not as harmless as it looked; run the on-device checks in full then.
- Review the Gradle files of every affected module. Look for dependencies nothing uses anymore,
  ones declared in the wrong configuration, and anything that could be expressed more simply.
- A task branch that touches `iosMain`, `iosApp/`, `config/swiftlint/` or a build file ends by
  asking the developer whether to run `ios.yml` on that branch, with `ios_tests=true`. See
  [ios/ci.md](ios/ci.md).
- A task that changes UI the iOS app shows, shared Compose UI in `commonMain` included, ends by
  offering an `ios.yml` run with `ui_flow=true`, on the devices and in the orientations it reaches.

## Review

- Finish with a maximally thorough code review of the change; it is mandatory. Dispatch subagents
  to review, then skeptic subagents to re-check what the reviewers reported. A finding counts only
  once a skeptic has confirmed it against the code. A dismissal counts only once a skeptic has
  failed to reproduce it.
- Every reviewer and skeptic prompt says: Read each changed file with the Read tool, not the
  shell, so its rules load. Read [committing.md](committing.md), this file, and the topic files
  the indexes name for the areas of the diff, deleted paths included. Read the design files the
  thin rules and [technical-design.md](technical-design.md) name for the diff's paths, and the
  files of the task's row in the task table of
  [.claude/design/TECHNICAL-DESIGN.md](../design/TECHNICAL-DESIGN.md). Flag code that departs from
  them, and an approach the change altered that the design
  still describes the old way.

## Wrapping up

- Document what the task changed before calling it done: the module README, the KDoc on the
  entities it points at, and the module's regression file. Call the `code-documentation` skill for
  it, and read [module-docs.md](module-docs.md).
- When the task changed how something is built (an approach, a convention, a file a design
  skeleton mirrors), update the technical design in the same task. Its maintenance notes are in
  [.claude/design/TECHNICAL-DESIGN.md](../design/TECHNICAL-DESIGN.md).
- Delete every artifact produced while verifying: screenshots, logcat dumps, UI hierarchy dumps,
  temporary scripts, anything created only to check the result. This covers the session scratchpad
  and the device/emulator alike, whether the check passed or failed. The iOS CI media under
  `~/Desktop/iOS test/` is the exception and is never deleted; see [ios/ci.md](ios/ci.md).
- Report back on every check from this list that was actually performed, so the developer sees
  what was verified without re-checking it.
