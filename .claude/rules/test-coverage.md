---
paths:
  - "**/.claude/rules/test-coverage.md"
  - "/build.gradle.kts"
  - "**/.claude/worktrees/*/build.gradle.kts"
  - "**/.worktrees/*/build.gradle.kts"
  - "**/src/*Test/**"
  - "**/src/test/**"
  - "**/fake/**"
  - "**/core-kmp/test-utils/**"
  - "**/iosApp/iosAppUITests/**"
---

# Test coverage

- Kover is the project's coverage tool. Measure with it rather than guessing from the diff.
- The bar is one number over the whole project, and it is enforced. `./gradlew koverVerify` fails
  the build when aggregated line coverage falls below `wholeProjectLineCoverageMinimum` in the root
  `build.gradle.kts`, currently 95%.
- The floor sits well below where the project is, on purpose. The gate catches a change that
  arrived with no tests at all, not a branch or two no test can reach.
- `koverVerify` is outside `check` and `build`, so an ordinary build never pays for it.
  [finishing-a-task.md](finishing-a-task.md) says when it runs.
- It is checked over the whole project on purpose. A module's own report counts only that module's
  test runs, so coverage from a neighboring module's tests is missing from it, and it reads low.
- While writing tests, look at the affected module alone: `./gradlew :<module>:koverLog` for the
  number, `:<module>:koverHtmlReport` for where the gaps are. Read those to find gaps, not to decide
  whether the bar is met.
- Where a module falls short in places you did not touch, neither fix it silently nor stay quiet.
  Name the uncovered parts, offer to cover them, and let the developer decide.
- Filtered out by the root build, so outside the number: code nobody wrote, `@Composable`
  functions, handwritten test doubles and `core-kmp:test-utils`. Generated code means what Room,
  kotlin-inject, KSP and the Compose compiler generate. That includes the `DefaultImpls` holders
  the Kotlin compiler copies an interface body into, and the `ComposableSingletons` holders behind
  composable lambdas.
- A handwritten `@Provides` body is not generated code and is measured like anything else. Add no
  filter that hides `Di*Component`: a test exercises its bindings directly.
- Keeping `@Composable` out is deliberate, not a gap to close. The Compose compiler expands a
  composable into synthetic lambda classes of about two lines each, so a percentage over them
  measures generated shapes, not tested behavior.
- That exclusion follows the annotation, not the package. Compose-adjacent code without
  `@Composable` is measured: `Modifier` extensions such as `repeatingClickable`, and token files
  like `Colors.kt` and `Fonts.kt`.
- Android platform code counts too; Kover measures it through `androidHostTest`.
- Kover cannot measure Kotlin/Native, so nothing in `iosMain` reaches the number. See
  [ios/tests.md](ios/tests.md) for what that means for placing logic.
