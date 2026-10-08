# Documentation

A map of what documents what in this project, and where the rules and skills for each kind live.
This file only points; each target owns its own content.

Read when: documenting a change; deciding where a piece of knowledge belongs; looking for a
README, a regression file, a planning doc or this design.

## Modules

- Each Gradle module, and the iOS host folder, carries a README and a regression file at its root.
  When they are created or updated, and how they are named, is in
  [module-docs.md](../rules/module-docs.md).
- The README is an index that points at the KDoc of the module's major entities. Both are written
  through the `code-documentation` skill:
  [.claude/skills/code-documentation/SKILL.md](../skills/code-documentation/SKILL.md).
- The skill's references hold worked examples: a finished README
  ([after-good.md](../skills/code-documentation/references/after-good.md)), the same README before
  tightening ([before-bad.md](../skills/code-documentation/references/before-bad.md)), the two
  side by side ([before-after.md](../skills/code-documentation/references/before-after.md)) and
  the store-shaped variants
  ([store-pattern.md](../skills/code-documentation/references/store-pattern.md)).
- Example: [MAIN-README.md](../../main/MAIN-README.md) and
  [MAIN-REGRESS.md](../../main/MAIN-REGRESS.md).

## Regression

- A module's regression file is its manual test script. What goes in it is set by
  [module-docs.md](../rules/module-docs.md) and the `code-documentation` skill.
- The full regression runs from `<APP>-FULL-REGRESS.md` in the project root, which links every
  module's regression file. Example: [ANOTI-FULL-REGRESS.md](../../ANOTI-FULL-REGRESS.md).
- A regression is walked, reported and triaged with the `manual-regression` skill:
  [.claude/skills/manual-regression/SKILL.md](../skills/manual-regression/SKILL.md). Its
  references cover the phase-two questions
  ([questions.md](../skills/manual-regression/references/questions.md)), the test scripts
  ([regression-files.md](../skills/manual-regression/references/regression-files.md)), the walk log
  ([walk-log.md](../skills/manual-regression/references/walk-log.md)), the report
  ([report.md](../skills/manual-regression/references/report.md)), the review briefs
  ([reviews.md](../skills/manual-regression/references/reviews.md)), Android emulators
  ([android-emulators.md](../skills/manual-regression/references/android-emulators.md)) and iOS
  simulators on CI
  ([ios-simulators-ci.md](../skills/manual-regression/references/ios-simulators-ci.md)).

## Code comments

- Comments and KDoc follow [code-comments.md](../rules/code-comments.md).
- Log lines, which a regression may search for by wording, follow
  [logging.md](../rules/logging.md).

## Project-level documents

- The root `README.md` is the GitHub-facing description. Its rule is in
  [CLAUDE.md](../../CLAUDE.md#git-and-github).
- Specs and plans for a larger task are local planning docs under `docs/superpowers/`. That folder
  is ignored by git, so nothing in the repository links it. How they fit into a task is in
  [.claude/design/development-workflow.md](development-workflow.md).
- This design is indexed by [.claude/design/TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md). Its upkeep
  is described there.
- The agent's memory lives outside the repository. What it holds and how it relates to the rules
  and this design is in [.claude/design/agent-setup.md](agent-setup.md).
