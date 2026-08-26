# CLAUDE.md

Read this before doing any task in this repository.

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

## Git commits

- Never add a `Co-Authored-By: Claude ...` trailer (or any co-author trailer) to commit
  messages.
- Commit message must fit on one line as shown in the GitHub/GitLab commit list (without body). If
  the change can't be summarized that briefly, use a short general phrase instead of trying to
  list everything — don't add a multi-line body to fit more detail in.

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
