# Development workflow

The order a task moves in, from its branch to its regression walk, as a map of links. Each step
is owned by the rule or skill it links; this file only puts them in sequence.

Read when: starting a task and planning its steps; before a commit, a push or a CI run; when
finishing a task and checking that no step was skipped.

## Steps

1. **Branch.** Work happens on its own git branch, never on `develop`; parallel work uses git
   worktrees: [CLAUDE.md "Branching"](../../CLAUDE.md#branching).
2. **Questions, spec, plan.** A large task gathers its questions first, then gets a spec and a
   plan, each reviewed before work starts. They are local planning docs under
   `docs/superpowers/specs/` and `docs/superpowers/plans/`, ignored by git.
3. **Design.** Before adding to or changing the structure of the code, read
   [TECHNICAL-DESIGN.md](TECHNICAL-DESIGN.md) and the files its task table names.
4. **Implementation and tests.** Tests follow [tests.md](../rules/tests.md),
   [test-coverage.md](../rules/test-coverage.md) and [testing.md](testing.md).
5. **Commit.** As work progresses, after the checks in [committing.md](../rules/committing.md).
   Message format: [CLAUDE.md "Git and GitHub"](../../CLAUDE.md#git-and-github).
6. **Finishing.** Correctness, tests, builds, the review and the wrap-up:
   [finishing-a-task.md](../rules/finishing-a-task.md). A task that changes no logic proves it
   with [no-logic-proof.md](../rules/android/no-logic-proof.md).
7. **Documentation.** Module README, KDoc and regression file:
   [module-docs.md](../rules/module-docs.md) and the
   [code-documentation](../skills/code-documentation/SKILL.md) skill. The documentation map is
   [documentation.md](documentation.md).
8. **CI.** Which workflow runs when, and how to start and read a run:
   [ci-github.md](../rules/ci-github.md), [android/ci.md](../rules/android/ci.md),
   [ios/ci.md](../rules/ios/ci.md).
9. **Push.** Only on the developer's word, every time:
   [CLAUDE.md "Git and GitHub"](../../CLAUDE.md#git-and-github).
10. **Regression walks.** A module's own `-REGRESS.md`, or the whole app from
    `<APP>-FULL-REGRESS.md` (Example: [ANOTI-FULL-REGRESS.md](../../ANOTI-FULL-REGRESS.md)),
    walked with the
    [manual-regression](../skills/manual-regression/SKILL.md) skill:
    [module-docs.md "Regression files"](../rules/module-docs.md#regression-files).
