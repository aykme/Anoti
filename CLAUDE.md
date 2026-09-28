# CLAUDE.md

Read this before doing any task in this repository.

The rules that belong to one platform live in two files next to this one. Both are part of
these rules and load together with them.

@CLAUDE-ANDROID.md

@CLAUDE-IOS.md

## Language

- Everything written into the project itself must be in English: code comments, KDoc,
  READMEs, commit messages, plans, specs, and any other artifact that ends up recorded in git.
- Communication with the developer in the console/chat is always in Russian, regardless of the
  language used in the project's own artifacts.

## Skills

- Before starting any task, check the available skills (this project's `.claude/skills/` as
  well as any user-level ones) and propose using one if it applies, rather than skipping
  straight to ad-hoc work a skill already covers.
- This project has a `code-documentation` skill at `.claude/skills/code-documentation/`,
  covering both module READMEs and KDoc/code-comment conventions. Use it before writing a
  README or documenting code — see "Module READMEs" below for when it must be called.

## Branching

- Never do task work directly on `develop`. Work must happen on a separate branch.
- If we're currently on `develop` and no dedicated branch has been created yet for the task,
  remind the developer of this before proceeding with the work.
- This refers to an actual git branch, not a worktree — an agent creating a worktree does not
  satisfy this requirement.
- When an agent performs tasks, maximize parallelism by using git worktrees wherever it's
  useful for isolating concurrent work.
- When finishing a task, if worktrees were used, clean them up once their branches have been
  merged.

## Code comments

- Comments and KDoc describe the current code, not the task that produced it. Never write what
  existed before the change, what didn't exist yet, or other task/migration history/status
  ("this used to be X", "doesn't exist in this repo yet", "tracked here so it isn't
  forgotten"). That belongs in the commit message or PR description, not in the code, and it
  rots the moment it's no longer true.
- Never compare the current code to a previous/removed implementation, even indirectly ("X used
  to draw this as...", "unlike the old View-based version..."). Describe only what the code in
  front of the reader does and why, as if no earlier version ever existed.
- Inline comments (not class/interface-level KDoc) are only for code that genuinely isn't
  self-evident from reading it — a hidden constraint, a race being guarded against, a
  workaround, a non-obvious magic value. If the code is clear on its own, add no comment at all.
- When a comment is warranted, keep it to the shortest phrase that states what and why — one
  line next to the non-obvious part, not a paragraph. Don't restate what the code already says,
  don't explore alternatives, don't explain the implementation step by step.
- The same brevity applies to KDoc on classes/interfaces/functions: a short "what this is and
  why it exists," not a walkthrough of how it's implemented.
- Keep individual sentences short — roughly 20 words, never around 40. The IDE flags long
  sentences as a readability issue. Split a long sentence into two short ones instead of joining
  clauses with commas/"and"/em dashes.

## Platform source sets (androidMain/iosMain)

- This is a KMP app. `commonMain` is the default location for everything; a platform source set
  (`androidMain`, `iosMain`) is only for code that is genuinely impossible to write in
  `commonMain` — a real platform API with no KMP equivalent (`Context`, `PendingIntent`,
  `NotificationChannel`, `WorkManager`, an actual `Activity`/`Application` subclass, and the
  like). It is not a place to retreat to because a KMP-compatible way of doing something wasn't
  worked out yet, and not a default reached for at the first sign of friction.
- Before adding or leaving anything in a platform source set, check concretely whether it
  actually needs a platform-only type anywhere in its own signature or body — an interface,
  data holder, or plain function with zero platform imports belongs in `commonMain`. Even if its
  only current implementer/caller happens to be platform-specific.
- This applies to Compose code too: a composable function only needs to live in `androidMain`/
  `iosMain` if it directly touches a platform-only API (e.g. a `View`/`ComposeView` bridge). A
  composable built entirely from `compose.runtime`/`compose.foundation`/`compose.material3` and
  other `commonMain` types belongs in `commonMain`, regardless of which platform currently calls
  it.
- When a platform-specific value or condition is needed inside otherwise-portable logic (e.g. an
  Android-only OS-version check), compute it in the platform layer and pass the *result* in as a
  plain parameter (a `Boolean`, a `Modifier`, a `Dp`) — don't let the platform-specific concept
  itself (its name, its reasoning) leak into the `commonMain` signature.
- Re-verify this placement whenever a task removes or restructures platform-specific code (e.g.
  a `Fragment`→Compose migration) — code that was platform-only because of something now-deleted
  (a `Fragment`, a `View`) often has no remaining reason to stay platform-specific and should
  move to `commonMain` as part of that same task, not be left behind.

## MVI stores

- Executors are pure orchestration. Any mutable data that reflects real application state (a
  flag, a counter, a "has this happened before" marker) must live in the Store's `State`, changed
  only through a `Message`/reducer — never as a bare `private var` field on the Executor.
- A `private var` on an Executor is acceptable only for non-observable coroutine plumbing that
  isn't itself application state (a `Job` handle, a `MutableStateFlow` used to debounce/trigger
  work, a `Paginator` instance) — never for anything the reducer or UI would otherwise need to
  reason about.
- When a decision needs "has this already happened", derive it from an existing `State` field
  (e.g. a `contentType` still at its untouched default) instead of introducing a dedicated
  tracking property for that one case.

## Compose design tokens (Dimens/Fonts/Colors/Const)

- Shared Compose UI constants live in typed files by kind: `Dimens.kt` (sizes, spacing,
  corner/alpha percentages — `Dp`/`Int`), `Fonts.kt` (text sizes — `TextUnit`), `Colors.kt`
  (the color palette). `Const.kt` is separate and holds only business-logic constants (paging,
  timing, domain limits) — never UI values.
- Placement: a constant used by more than one module lives in the closest common dependency
  every consumer already has (e.g. `core-kmp:celebrity` for values needed project-wide,
  `feature-kmp:anime-base` for values shared only among the anime feature screens that already
  depend on it). A constant used by exactly one module lives in a local file of the same kind
  inside that module's own package instead — don't leave it dangling as a bare `private const
  val` at the bottom of a UI/Composable file.
- When more than one module has its own local `Dimens.kt`/`Fonts.kt`, prefix the file name with
  the module name (e.g. `AnimeListDimens.kt`, `AnimeFavoritesDimens.kt`) so it's unambiguous
  which one an import/search is referring to.
- Comments on these constants describe what the value represents, not which feature(s) or
  screen(s) currently consume it — that list can change independently of the value itself and
  shouldn't be hardcoded into the comment.

## Git commits

- Never add a `Co-Authored-By: Claude ...` trailer (or any co-author trailer) to commit
  messages.
- Commit message must fit on one line as shown in the GitHub/GitLab commit list (without body). If
  the change can't be summarized that briefly, use a short general phrase instead of trying to
  list everything — don't add a multi-line body to fit more detail in.

## Tests

- Structure every test body into three clearly separated sections marked with `//Given`,
  `//When`, `//Then` comments, even for a short test — this is the established convention across
  the existing test suites (e.g. `SafeApiImplTest`) and keeps setup, action, and assertion
  visually distinct.
- Where a library already mocks the thing being stubbed, use it rather than writing a double by
  hand — Ktor's `MockEngine` for an HTTP client is the example. No general-purpose mocking
  framework is used, though: `commonTest` targets Kotlin/Native (iOS) alongside Android, and none
  of them run there. Everything a library does not cover is a handwritten fake.
- **A double is never duplicated.** One class per thing being faked, across the whole repository.
  Two copies are unacceptable even when their bodies differ — the one class takes on what both
  needed. A double used by more than one module lives in `commonMain` of the module that owns the
  type it stands in for, never in a test source set, since a test source set is invisible to other
  modules.
- **`Fake` goes at the end of the name, never at the front.** `SafeApi` is faked by `SafeApiFake`,
  not by `FakeSafeApi`. This holds for every double without exception, down to a `private` one
  nested inside a single test class. A name saying what the double does rather than what it
  stands in for — `NoOpSource`, `HangingSource`, `RecordingUsecase` — still ends in `Fake`:
  `NoOpSourceFake`, `HangingSourceFake`, `RecordingUsecaseFake`.
- The suffix is also what keeps the class out of the coverage numbers, since the Kover filter
  matches on it. Getting the name wrong therefore skews the measurement, not just the reading.
- Put the class in the package matching the real type's, with `fake` as the last package segment
  — `impl/data/fake/SafeApiFake.kt` alongside `api/data/SafeApi.kt`. One double per file, named
  after it. A double `private` to one test class is the exception: it stays nested where it is
  and only the name has to follow. Give it a file in a `fake` package the moment a second test
  needs it.
- A double used by exactly one module still follows the naming, and stays in that module's own
  test source set until a second module needs it.
- `commonTest` is the default home for a test, and shared code stays the priority when effort has
  to be split. Platform code is covered too: an Android implementation gets its tests in
  `androidHostTest`, an iOS one in `iosTest` — the pair of `AnimeDatabaseContinuityTest` classes
  in `core-kmp:anime-database` shows the shape.
- An `iosTest` runs on macOS and nowhere else: on this machine `iosSimulatorArm64Test` is skipped
  outright, so a test there is compiled and never executed. Kover cannot measure Kotlin/Native
  either. Such a test proves nothing here and counts for nothing.
- So before writing one, check whether the code under it needs an iOS API at all. A class built
  from coroutines, atomics and the module's own types belongs in `commonMain`, where its test
  runs on every build and is measured — even when iOS is its only caller. `BackgroundRefreshPass`
  and `BackgroundRefreshTask` in `feature-kmp:anime-background-update` show the shape: the
  portable half of a background refresh is common and tested, and `AnimeBackgroundSchedulerImpl`
  is left holding the `BGTaskScheduler` calls and nothing else.
- What stays in `iosTest` is what only a real iOS runtime can answer, and it is written knowing
  it will not run until someone builds on a Mac.
- Where both platforms need the same thing built — wording, an id, a format — build it once in
  `commonMain` and have both call it. Two copies drift, and review is not what should be holding
  them together. `newEpisodeNotificationText` in `feature-kmp:anime-notification` is that shape.
- A module that is not multiplatform keeps its host tests in `src/test/kotlin`, and its
  instrumented tests in `src/androidTest/kotlin`. Never a `java` directory, in any source set.
- Composable tests, the app's own `Application`, instrumented tests and the SDK level
  Robolectric emulates have rules of their own. See "Tests on Android" in `CLAUDE-ANDROID.md`.
- The code under test is the real thing, wiring included; what it reaches for is where the fakes
  start. A test may build a real DI component, as long as everything handed to that component is
  a handwritten fake: no real database, no network, no background work.
- Drive time and concurrency through the test infrastructure rather than the real thing: `runTest`
  and its virtual clock, `advanceTimeBy`/`advanceUntilIdle`, and `UnconfinedTestDispatcher` or
  `StandardTestDispatcher` installed via `Dispatchers.setMain` — all already established across
  the existing suites. The same goes for threads: a test dispatcher, never a real one.

## Test coverage

- Kover is the project's coverage tool — measure with it rather than guessing from the diff.
- The bar is one number over the whole project, and it is enforced. `./gradlew koverVerify` fails
  the build when aggregated line coverage falls below `wholeProjectLineCoverageMinimum` in the
  root `build.gradle.kts`, currently 95%.
- The floor is not where the project sits — it holds a good deal more. The room between the two
  is deliberate: the gate is there to catch a change that arrived with no tests at all, not a
  branch or two no test can reach. Cover what you write regardless; the number is a safety net,
  not the standard.
- Nothing runs the task for you. It is deliberately outside `check` and `build`, so an ordinary
  build never pays for it — "Finishing a task" below says when to run it.
- It is checked over the project as a whole on purpose. A module's own report counts only that
  module's test runs, so the coverage its classes get from a neighboring module's tests is
  missing from it, and it reads lower than the truth.
- While writing tests, still look at the affected module alone — `./gradlew :<module>:koverLog`
  for the number, `:<module>:koverHtmlReport` for where the gaps are. Read those to find gaps,
  not to decide whether the bar is met.
- Where a module falls short in places you did not touch, neither fix it silently nor stay quiet.
  Name the uncovered parts, offer to cover them, and let the developer decide.
- Filtered out of the measurement by the root build, and therefore outside the number: code
  nobody wrote — what Room, kotlin-inject, KSP and the Compose compiler generate, including the
  `DefaultImpls` holders the Kotlin compiler copies an interface body into and the
  `ComposableSingletons` holders behind composable lambdas — plus `@Composable` functions,
  handwritten test doubles and `core-kmp:test-utils`.
- A handwritten `@Provides` body is not generated code and is measured like anything else. A
  filter that hid every `Di*Component` also hid the bindings a test exercises directly, which is
  why there is no such filter.
- Keeping `@Composable` out is deliberate, not a gap waiting to be closed. The Compose compiler
  expands a composable into synthetic lambda classes (`...Kt$name$1$1$1`) of about two lines each,
  so a percentage over them measures generated shapes rather than tested behavior. Write the
  tests, judge them by what they assert, and ignore the number.
- That exclusion follows the annotation, not the package. Compose-adjacent code without
  `@Composable` — `Modifier` extensions such as `repeatingClickable`, and token files like
  `Colors.kt` and `Fonts.kt` — is measured like anything else.
- Android platform code counts too; Kover measures it through `androidHostTest`.
- Kover cannot measure Kotlin/Native, so nothing in `iosMain` reaches the number. Together with
  `iosTest` not running off macOS, that is the reason logic belongs in `commonMain` — see
  "Tests" above.
- The minimum is a floor, never a finish line. Hitting the percentage is not the goal: cover the
  main cases, the risky ones, the bottlenecks and the boundaries. Where concurrency is real,
  cover races and ordering as well. A test written only to move the number is worse than no test.

## Finishing a task

- This is a final stage — run it at the point the task is being finished, e.g. during a final
  code review or the final step of the work — not after each individual subtask generated along
  the way. Don't run through this checklist after every subtask.
- Verify that the logic you wrote actually works correctly — don't just assume it from reading
  the code.
- If the task was a refactor or a migration, ask the developer whether the resulting behavior
  must match the previous behavior exactly. If yes, verify there's no difference in the final
  result; if some difference turns out to be unavoidable, agree on it with the developer before
  proceeding.
- Make sure the tests cover every case that can realistically occur, without duplicate tests or
  clearly excessive coverage that adds nothing. Don't forget concurrency tests where they're
  needed.
- Run every test in each affected module, not only the ones this task wrote, and confirm they are
  all green. Then take the "Tests" section above rule by rule against what the module now holds,
  and fix whatever doesn't conform.
- On Android the instrumented tests are part of that run, and every check that needs the app
  running belongs on an emulator. See "Instrumented tests and the emulator" in
  `CLAUDE-ANDROID.md`.
- For any test that's new or was fixed, confirm it doesn't flake, doesn't rely on real time
  (highly undesirable — acceptable only in exceptional cases agreed with the developer), and
  never makes real API calls (this is forbidden).
- Measure the coverage of every module you touched instead of estimating it from the diff.
  `./gradlew :<module>:koverLog` prints the number; `:<module>:koverHtmlReport` shows where the
  gaps are. Name what is still uncovered rather than staying quiet about it.
- Then run `./gradlew koverVerify` and get it green, before the code review below. Nothing else
  in the build runs it, and it is the gate the whole project is held to — see "Test coverage"
  above. A red one is not reported as a finding, it is fixed.
- Check the change still works once R8 has had it — see "R8 and the minified build" in
  `CLAUDE-ANDROID.md`.
- A task that changes no logic skips every on-device check: the instrumented tests, the
  minified walk and the manual regression. A rename, a file move, and an edit to comments or
  documentation are such tasks.
- Such a task proves itself another way: a build made before the change is compared with one
  made after it. What is compared on Android, and what may differ, is in "Proving a task that
  changes no logic" in `CLAUDE-ANDROID.md`.
- A difference beyond what that section allows means the change was not as harmless as it
  looked. Run the on-device checks in full then.
- Review the Gradle files of every affected module. Look for dependencies nothing uses anymore,
  ones declared in the wrong configuration, and anything that could be expressed more simply.
- Finish with a maximally thorough code review of the change. This one is mandatory. Dispatch
  subagents to do the reviewing, then dispatch skeptic subagents to re-check what the reviewers
  reported. A finding counts only once a skeptic has confirmed it against the code, and a
  dismissal counts only once a skeptic has failed to reproduce it.
- Document what the task changed before calling it done: the module README, the KDoc on the
  entities it points at, and the module's regression file. Call the `code-documentation` skill
  for it — see "Module READMEs" and "Module regression files" below.
- Delete every artifact produced while verifying — screenshots, logcat dumps, UI hierarchy dumps,
  temporary scripts, and anything else created only to check the result. This applies to the
  session scratchpad and to the device/emulator alike. Nothing of the sort is left behind once
  the check is done, whether it passed or failed.
- Report back on every check from this list that was actually performed, so the developer can
  see what was verified without having to re-check it themselves.

## Before committing

- Everything below applies only to the files actually being committed. If other files were
  touched or are otherwise affected but aren't part of this commit, none of this applies to
  them.
- Go through every changed file before committing:
    - For Kotlin files: run detekt on the files being committed. Fix whatever it flags, then
      run detekt again on those same files to confirm the fixes actually resolved the issues.
    - If a finding is easy to fix without changing logic (formatting, naming, straightforward
      extraction, and the like), fix it yourself. If resolving a finding would require a
      substantial change to the logic, don't guess — ask the developer which approach to take.
    - Nothing deprecated goes in. Read the compiler's deprecation warnings for the files being
      committed and clear every one, in test code as much as in production code. Where a
      replacement exists, use it; where none does, ask the developer rather than suppressing the
      warning. This covers third-party APIs too, not just the project's own.
    - For files detekt doesn't analyze (`*.md`, `*.xml`, and similar), do the equivalent by
      hand: reformat the code, optimize imports, and check that formatting matches the
      codebase's established conventions.
- This applies to `README.md` and every other `*.md` file in the repo, including skill files
  under `.claude/skills/**` — try to keep them as clean as source code.
- This also applies to Gradle files (`build.gradle.kts`, `settings.gradle.kts`, and similar) and
  to `*.toml` files, including `gradle/libs.versions.toml` — they're code too, so reformat them
  and check that their formatting matches established conventions the same as any other file.
- When the commit writes or changes Compose UI, run the Compose compiler reports over it:
  `./gradlew :androidApp:assembleDebug -PcomposeCompilerReports`. They land in
  `<module>/build/compose_compiler/`, and only modules that recompiled get fresh files. Read them
  for the entities being committed, not for the whole project:
    - every `restartable` composable must also be `skippable`;
    - no composable parameter is an `unstable` type you introduced;
    - UI models read `stable class`, not `runtime class`. A model that misses it only because of
      a generic (e.g. `ImmutableList<T>`) gets `@Immutable`. Annotate only when every property is
      a `val` that never changes after construction.
- Compose annotations stay out of domain types. A `@Stable` interface in `api/domain` leaks the
  UI layer into it. Leave it alone and note the report entry instead.

## Module READMEs

- Whenever a module is created or changed, create (if missing) or update its README to
  reflect the change, and finish documenting (KDoc) the entities it points to. Its regression
  file is updated in the same pass — see "Module regression files" below.
- Call the `code-documentation` skill (`.claude/skills/code-documentation/`) once a module's
  changes are otherwise finished — documenting it is part of finishing the task, not a
  separate follow-up to do later.
- READMEs are only for KMP modules: `core-kmp/*`, `feature-kmp/*` and `main`. A module that is
  not multiplatform, such as `androidApp`, doesn't get one.
- File name: the module's full Gradle path, uppercase, colons replaced with dashes, suffixed
  `-README.md` (e.g. `:core-kmp:celebrity` → `CORE-KMP-CELEBRITY-README.md`), placed at the
  module's root.

## Module regression files

- Every module carries a regression file at its root, named like its README but ending `-REGRESS.md`
  (e.g. `:core-kmp:celebrity` → `CORE-KMP-CELEBRITY-REGRESS.md`).
- It is the module's manual test script: only what cannot be checked from the code, written for a
  tester who has never seen it. Anything provable from the code belongs in a test instead.
- A step is written for the app rather than for one platform. Where the same check can be walked
  on iOS as on Android, it is one step phrased for both; where only one platform can reach it,
  the step names that platform and says why.
- It is written and updated through the `code-documentation` skill, in the same pass as the
  module's README. The skill holds the rules for what goes in it and how far its scope reaches.
- A regression **of one module** is run from that module's own `-REGRESS.md`, at the module's
  root.
- A regression **of the whole app** is run from `ANOTI-FULL-REGRESS.md` in the project root. It
  carries no steps of its own — it links to every module's regression file, and a full run
  means working through all of them.
- Creating or deleting a module's regression file includes adding or removing its link in
  `ANOTI-FULL-REGRESS.md`. A file that exists but is not linked is skipped silently by a full
  regression.

## Root README.md

The root `README.md` is the GitHub-facing project description. It does not follow the module
README rules above, and must not be touched unless explicitly asked.
