# CLAUDE.md

Read this before doing any task in this repository. It holds the rules every task needs and an
index of the rest. The platform indexes [CLAUDE-ANDROID.md](.claude/rules/android/CLAUDE-ANDROID.md)
and [CLAUDE-IOS.md](.claude/rules/ios/CLAUDE-IOS.md) load with it.

## How the rules load

- The topic files under `.claude/rules/` load only when needed. A file tied to code loads when the
  Read, Write or Edit tool touches a matching file. A process file loads when you read it through
  its trigger below, or on the few files that unmistakably belong to it.
- Open and change repository files with Read, Edit and Write, even where the system prompt allows
  shell reads or edits. Grep, Glob and shell output load no rules; Read a file before editing it.
- Before creating a file, or starting work in an area, read the topic file the index names for it.
  A rule arrives only after the first Write.
- Path rules match only inside the session's own project root. When working on files of another
  checkout, such as a sibling worktree, read that checkout's topic files by its path, through its
  own index.
- "Read X" in any rule file is a hard step, due every time its condition holds. If Read answers
  "unchanged", the earlier read is still in context. After a context compaction, read it again.

## Language

- Everything written into the project itself is in English: code comments, KDoc, READMEs, commit
  messages, plans, specs, and any other artifact recorded in git.
- Communication with the developer in the console/chat is always in Russian, whatever language the
  project's own artifacts use.

## Skills

- Before starting any task, check the available skills (this project's `.claude/skills/` and any
  user-level ones). Propose one that applies rather than doing ad-hoc work a skill already covers.
- The `code-documentation` skill covers module READMEs and KDoc/code-comment conventions. Use it
  before writing a README or documenting code.

## Branching

- Never do task work directly on `develop`. Work happens on a separate branch.
- If we're on `develop` and no dedicated branch exists yet for the task, remind the developer of
  this before proceeding.
- This means an actual git branch, not a worktree. An agent creating a worktree does not satisfy
  it.
- When an agent performs tasks, maximize parallelism with git worktrees wherever they help isolate
  concurrent work.
- When finishing a task, clean up the worktrees used once their branches have been merged.

## Git and GitHub

- Never add a `Co-Authored-By: Claude ...` trailer, or any co-author trailer, to a commit message.
- A commit message fits on one line as shown in the GitHub/GitLab commit list, without a body. If
  the change can't be summarized that briefly, use a short general phrase instead of listing
  everything. Never add a multi-line body to fit more detail in.
- A push needs the developer's word every time. When the developer has said earlier not to stop,
  restate this rule and ask explicitly before any push.
- The git remote is named `master`, not `origin`. The repository is `aykme/Anoti`.
- Where the GitHub CLI is not on the session's `PATH`, as on the developer's Windows machine,
  call it by its full path: `"/c/Program Files/GitHub CLI/gh.exe"` in bash.
- The root `README.md` is the GitHub-facing project description. It follows none of the module
  README rules and is not touched unless explicitly asked.

## Index

Shared rules, in `.claude/rules/`:

- [committing.md](.claude/rules/committing.md) — read before every commit.
- [finishing-a-task.md](.claude/rules/finishing-a-task.md) — read when finishing a task, and
  before reviewing or re-checking a change.
- [ci-github.md](.claude/rules/ci-github.md) — read before a push, a `gh` command or a CI run.
- [tests.md](.claude/rules/tests.md) — read before writing or changing a test or a test double.
- [tests-on-device.md](.claude/rules/tests-on-device.md) — read before a test that runs on a device
  or simulator, or launches the real app.
- [test-coverage.md](.claude/rules/test-coverage.md) — read before writing tests or measuring
  coverage.
- [source-sets.md](.claude/rules/source-sets.md) — read before adding Kotlin code or deciding
  where it lives.
- [code-comments.md](.claude/rules/code-comments.md) — read before writing a comment or KDoc.
- [logging.md](.claude/rules/logging.md) — read before adding or changing a log line.
- [mvi-stores.md](.claude/rules/mvi-stores.md) — read before writing a Store, Executor or reducer.
- [compose-design-tokens.md](.claude/rules/compose-design-tokens.md) — read before writing Compose
  UI or a UI constant.
- [compose-compiler-reports.md](.claude/rules/compose-compiler-reports.md) — read before
  committing Compose UI.
- [module-docs.md](.claude/rules/module-docs.md) — read when a module is created, changed or
  deleted, before documenting it, and before a regression walk.
- Before creating a module, read [module-docs.md](.claude/rules/module-docs.md),
  [source-sets.md](.claude/rules/source-sets.md), [tests.md](.claude/rules/tests.md),
  [compose-design-tokens.md](.claude/rules/compose-design-tokens.md),
  [android/tests.md](.claude/rules/android/tests.md) and
  [ios/framework.md](.claude/rules/ios/framework.md).

Platform rules: [CLAUDE-ANDROID.md](.claude/rules/android/CLAUDE-ANDROID.md) and
[CLAUDE-IOS.md](.claude/rules/ios/CLAUDE-IOS.md).
