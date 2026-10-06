---
paths:
  - "**/.claude/rules/committing.md"
---

# Before committing

The commit message rules (no co-author trailer, one line) are in [CLAUDE.md](../../CLAUDE.md).

- Everything below applies only to the files actually being committed. Files touched or affected
  but not part of this commit are out of scope.
- Go through every changed file before committing:
    - Kotlin files: run detekt on the files being committed. Fix whatever it flags, then run detekt
      again on those same files to confirm the fixes resolved the issues.
    - SwiftLint is due when the commit adds or changes a `*.swift` file, `config/swiftlint/
      swiftlint.yml`, `iosApp/scripts/swiftlint.sh`, or a `swiftLint*` entry of the catalog.
      Nothing else makes it due. Then read [ios/swiftlint.md](ios/swiftlint.md) and run it as it
      says.
    - A finding easy to fix without changing logic (formatting, naming, straightforward extraction,
      and the like): fix it yourself. If resolving it would need a substantial change to the logic,
      don't guess; ask the developer which approach to take.
    - Nothing deprecated goes in. Read the compiler's deprecation warnings for the files being
      committed and clear every one, in test code as much as in production code. Where a
      replacement exists, use it; where none does, ask the developer rather than suppressing the
      warning. Third-party APIs count too, not just the project's own.
    - Files detekt doesn't analyze get the same care by hand: reformat them, optimize imports, and
      check that formatting matches the codebase's conventions. Keep them as clean as source code.
      That covers every `*.md` (`README.md`, `.claude/skills/**`, `.claude/rules/**`), every
      Gradle file (`build.gradle.kts`, `settings.gradle.kts`, and similar), every `*.toml`
      (`gradle/libs.versions.toml`), every `*.xml`, and the like.
- When the commit writes or changes Compose UI, read
  [compose-compiler-reports.md](compose-compiler-reports.md) and run the reports as it says.
