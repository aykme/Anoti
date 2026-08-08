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

## Before committing

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
