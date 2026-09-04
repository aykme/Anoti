# CLAUDE.md

Read this before doing any task in this repository.

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
- No mocking library is used in this project: `commonTest` targets Kotlin/Native (iOS) alongside
  Android, and hand-written fakes (as already used throughout `commonTest`, e.g. `FakeOngoingSource`
  in `OngoingSectionExecutorImplTest`) are the established, KMP-portable way to stub dependencies.

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
  needed. Tests are only written for KMP code, and all of them belong in `commonTest` — don't
  write platform-specific tests, unless an exception is made for them.
- Run the tests in every affected module and confirm they're all green.
- When running UI (instrumented/`androidTest`) tests, always do a clean installation of the app
  first — uninstall it from the device/emulator before installing and running, so a stale build
  doesn't mask a failure or fake a pass.
- For any test that's new or was fixed, confirm it doesn't flake, doesn't rely on real time
  (highly undesirable — acceptable only in exceptional cases agreed with the developer), and
  never makes real API calls (this is forbidden).
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
    - For files detekt doesn't analyze (`*.md`, `*.xml`, and similar), do the equivalent by
      hand: reformat the code, optimize imports, and check that formatting matches the
      codebase's established conventions.
- This applies to `README.md` and every other `*.md` file in the repo, including skill files
  under `.claude/skills/**` — try to keep them as clean as source code.
- This also applies to Gradle files (`build.gradle.kts`, `settings.gradle.kts`, and similar) and
  to `*.toml` files, including `gradle/libs.versions.toml` — they're code too, so reformat them
  and check that their formatting matches established conventions the same as any other file.

## Module READMEs

- Whenever a module is created or changed, create (if missing) or update its README to
  reflect the change, and finish documenting (KDoc) the entities it points to.
- Call the `code-documentation` skill (`.claude/skills/code-documentation/`) once a module's
  changes are otherwise finished — documenting it is part of finishing the task, not a
  separate follow-up to do later.
- READMEs are only for KMP modules (`core-kmp/*`, `feature-kmp/*`). Non-KMP modules
  (app-level modules such as `app`/`main`, etc.) don't get one.
- File name: the module's full Gradle path, uppercase, colons replaced with dashes, suffixed
  `-README.md` (e.g. `:core-kmp:celebrity` → `CORE-KMP-CELEBRITY-README.md`), placed at the
  module's root.

## Root README.md

The root `README.md` is the GitHub-facing project description. It does not follow the module
README rules above, and must not be touched unless explicitly asked.
