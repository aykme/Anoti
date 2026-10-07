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
- A user-level skill written for another codebase does not apply here, whatever its description
  matches: the `multimodule-architecture`, `navigation`, `network-layer`, `testing`,
  `feature-flags`, `checks`, `qa-engineer`, `dependency-diagrams` and `network-baseline-capture`
  plugins describe other projects. This project's technical design and rules decide how code is
  written here.
- The `code-documentation` skill covers module READMEs and KDoc/code-comment conventions. Use it
  before writing a README or documenting code.

## Technical design

- [.claude/design/TECHNICAL-DESIGN.md](.claude/design/TECHNICAL-DESIGN.md) holds how code is
  written here: modules, entities, KMP, DI, navigation, saved state, UI, tests and docs, and how to
  start a new project in this image. Rules say what to do and when; the design says how to write.
- Read it before adding to or changing the structure of the code: a module, class, interface,
  object, DI binding, destination, store, library, resource or saved state. Read it before starting
  a new project too. Then read the files its task table names, in order. Both are hard steps.
- A rule that sends you to a design file counts the same: once it loads and the task does what its
  bullet names, read that design file before writing.
- Such rules arrive while you work, often right after a read or a write in a new area. When one
  arrives, read its design file before your next write in that area, even mid-task. A task that
  spans several areas (a store, its UI, DI, tests) reads the design file of each.
- Read the design files yourself before writing. A subagent's summary does not replace them, and
  a subagent sent to write or review code gets the design files of its task in its prompt.
- Before creating a module, read [new-module.md](.claude/design/new-module.md) and every file its
  section "Before the first file" names, then [module-docs.md](.claude/rules/module-docs.md).

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
- [tests.md](.claude/rules/tests.md) — read before writing or changing a test or a test double;
  it sends you to the design's testing file.
- [tests-on-device.md](.claude/rules/tests-on-device.md) — read before a test that runs on a device
  or simulator, or launches the real app.
- [test-coverage.md](.claude/rules/test-coverage.md) — read before writing tests or measuring
  coverage.
- [source-sets.md](.claude/rules/source-sets.md) — read before adding Kotlin code or deciding
  where it lives; it sends you to the design's KMP file.
- [code-comments.md](.claude/rules/code-comments.md) — read before writing a comment or KDoc.
- [logging.md](.claude/rules/logging.md) — read before adding or changing a log line.
- [mvi-stores.md](.claude/rules/mvi-stores.md) — read before writing a Store, Executor or reducer;
  it sends you to the design's MVI file.
- [compose-design-tokens.md](.claude/rules/compose-design-tokens.md) — read before writing Compose
  UI or a UI constant; it sends you to the design's Compose UI file.
- [technical-design.md](.claude/rules/technical-design.md) — read before creating or editing a
  file in a `di/`, `navigation/`, `data/`, `usecase/`, `savedstate/` or `composeResources/`
  package, a manifest, a `*Worker.kt` or a build file; it names the design file of each.
- [compose-compiler-reports.md](.claude/rules/compose-compiler-reports.md) — read before
  committing Compose UI.
- [module-docs.md](.claude/rules/module-docs.md) — read when a module is created, changed or
  deleted, before documenting it, and before a regression walk.

Platform rules: [CLAUDE-ANDROID.md](.claude/rules/android/CLAUDE-ANDROID.md) and
[CLAUDE-IOS.md](.claude/rules/ios/CLAUDE-IOS.md).
