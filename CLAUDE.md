# CLAUDE.md

Read this before doing any task in this repository.

## Skills

- Before starting any task, check the available skills (this project's `.claude/skills/` as
  well as any user-level ones) and propose using one if it applies, rather than skipping
  straight to ad-hoc work a skill already covers.
- This project has a `code-documentation` skill at `.claude/skills/code-documentation/`,
  covering both module READMEs and KDoc/code-comment conventions. Use it before writing a
  README or documenting code — see "Module READMEs" below for when it must be called.

## Git commits

- Never add a `Co-Authored-By: Claude ...` trailer (or any co-author trailer) to commit
  messages.
- Commit message must fit on one line as shown in the GitHub/GitLab commit list (without body). If
  the change can't be summarized that briefly, use a short general phrase instead of trying to
  list everything — don't add a multi-line body to fit more detail in.

## After finishing a task

- This is a final stage that runs only once the overall task is fully done — not after each
  individual subtask generated along the way. Don't run through this checklist after every
  subtask.
- Verify that the logic you wrote actually works correctly — don't just assume it from reading
  the code.
- If the task was a refactor, ask the developer whether the resulting behavior must match the
  previous behavior exactly. If yes, verify there's no difference in the final result; if some
  difference turns out to be unavoidable, agree on it with the developer before proceeding.
- Make sure the tests cover every case that can realistically occur, without duplicate tests or
  clearly excessive coverage that adds nothing. Don't forget concurrency tests where they're
  needed. Tests are only written for KMP code, and all of them belong in `commonTest` — don't
  write platform-specific tests, unless an exception is made for them.
- Run the tests in every affected module and confirm they're all green.
- For any test that's new or was fixed, confirm it doesn't flake, doesn't rely on real time
  (highly undesirable — acceptable only in exceptional cases agreed with the developer), and
  never makes real API calls (this is forbidden).

## Before committing

- Everything below applies only to the files actually being committed. If other files were
  touched or are otherwise affected but aren't part of this commit, none of this applies to
  them.
- Go through every changed file before committing:
  - If the project has a static code analyzer/linter configured, run it, fix what it flags,
    then run it again to confirm the fixes actually resolved the issues.
  - If there's no static analyzer for a given file, do the equivalent by hand: reformat the
    code, optimize imports, and check that formatting matches the codebase's established
    conventions.
- Regardless of whether a static analyzer exists, also go through every warning the IDE reports
  on changed files, of any severity (red, yellow, and weak/green hints alike) — not just
  compiler errors. This includes spelling/grammar warnings in prose and comments: fix them when
  the issue is clearly a real mistake; if there's genuine doubt, or the fix would mean adding a
  word to a dictionary, ask the developer instead of guessing.
  - Check this with the `mcp__ide__getDiagnostics` tool, called with `uri` set to each changed
    file's `file:///<absolute-path>`. Do this for every file being committed, not a sample —
    including files that feel trivial (a one-line docs edit, a `CLAUDE.md`/skill-file change, a
    Gradle tweak) and files in a commit that's mostly about something else. "This one's small" or
    "I already verified it a different way (e.g. the build passed)" is not a reason to skip it —
    a successful build proves the code compiles, it does not prove there's no IDE-only inspection
    warning (unused import, unnecessary `@Suppress`, a style hint), which is a different and
    non-overlapping check.
  - **The `getDiagnostics` call (with its retry, if needed) is the last tool call before
    `git commit`, every time, with no exceptions carved out for any category of file.** If
    something happens after that call and before the commit — another edit, a build, a detour —
    the check is stale; call it again immediately before committing, don't rely on an earlier
    call in the same turn.
  - If the tool times out or errors instead of returning diagnostics, that's an infrastructure
    problem, not a signal the file is clean — don't treat a timeout as "no warnings". Retry once;
    if it still fails, say so explicitly instead of silently skipping the check or committing
    anyway.
  - **Hard rule, no exceptions: do not commit a file with any diagnostic still open on it.**
    "It's expected"/"it's inherent to this task"/"nothing calls this yet" are not valid reasons
    to leave a warning unresolved — they're reasons to pick the correct fix (e.g. an explicit,
    justified `@Suppress` with a comment saying why, not a silent shrug), not a reason to skip
    fixing it. An unused-but-intentionally-public-API warning gets suppressed explicitly with a
    comment explaining why it's intentional; it does not get left as a bare warning because the
    reason is "obvious" to whoever wrote the code.
- This applies to `README.md` and every other `*.md` file in the repo, including skill files
  under `.claude/skills/**` — try to keep them as clean as source code.
- This also applies to Gradle files (`build.gradle.kts`, `settings.gradle.kts`, version catalog
  TOML files, and similar) — they're code too, not exempt from the reformat/warnings pass.
- If fixing a warning leads to an unresolvable contradiction (e.g. two conventions that can't
  both be satisfied), or the fix is a change that could actually break something rather than
  just quiet the IDE, stop and ask the developer instead of guessing which way to go.
- Any global change, or a version bump/change (dependency, Gradle, Kotlin, etc.) — even one
  suggested by a linter or an IDE quick-fix — also needs the developer's sign-off before you
  make it; don't fold it silently into a warnings-cleanup pass.
- Exception: example content inside a skill (illustrative code snippets, example READMEs meant
  to demonstrate a pattern) is allowed to trip warnings about unresolved packages/paths or
  similar — that's inherent to being a standalone example, not a bug. As a rule, don't edit
  example content just to silence IDE noise; leave examples alone unless the warning points to
  a genuine, fixable problem in the surrounding prose.

## Module READMEs

- Whenever a module is created or changed, create (if missing) or update its README to
  reflect the change, and finish documenting (KDoc) the entities it points to.
- Call the `code-documentation` skill (`.claude/skills/code-documentation/`) once a module's
  changes are otherwise finished — documenting it is part of finishing the task, not a
  separate follow-up to do later.
- READMEs are only for KMP modules (`core-kmp/*`, `feature-kmp/*`). Non-KMP modules
  (`core-platform/*`, `feature-platform/*`, app-level modules, etc.) don't get one.
- File name: the module's full Gradle path, uppercase, colons replaced with dashes, suffixed
  `-README.md` (e.g. `:core-kmp:celebrity` → `CORE-KMP-CELEBRITY-README.md`), placed at the
  module's root.

## Root README.md

The root `README.md` is the GitHub-facing project description. It does not follow the module
README rules above, and must not be touched unless explicitly asked.
