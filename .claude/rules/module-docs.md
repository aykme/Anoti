---
paths:
  - "**/.claude/rules/module-docs.md"
  - "**/*-README.md"
  - "**/*-REGRESS.md"
  - "**/settings.gradle.kts"
---

# Module READMEs and regression files

The root `README.md` follows none of this; see [CLAUDE.md](../../CLAUDE.md).

When creating a module, read [.claude/design/new-module.md](../design/new-module.md) first. It
holds how a module is built; this file holds its documentation.

## READMEs

- Whenever a module is created or changed, create (if missing) or update its README to reflect
  the change, and finish the KDoc of the entities it points to. Its regression file is updated in
  the same pass.
- Both are written through the `code-documentation` skill (`.claude/skills/code-documentation/`),
  called once a module's changes are otherwise finished. Documenting is part of finishing the task,
  not a follow-up.
- Every Gradle module gets a README: `androidApp`, `main`, `core-kmp/*` and `feature-kmp/*`.
- File name: the module's full Gradle path, uppercase, colons replaced with dashes, suffixed
  `-README.md` (e.g. `:core-kmp:celebrity` → `CORE-KMP-CELEBRITY-README.md`), at the module's root.
- `iosApp/` is not a Gradle module and still gets a README and a regression file, named after the
  folder: `iosApp/IOSAPP-README.md` and `iosApp/IOSAPP-REGRESS.md`.

## Regression files

- Every module carries a regression file at its root, named like its README but ending
  `-REGRESS.md` (e.g. `:core-kmp:celebrity` → `CORE-KMP-CELEBRITY-REGRESS.md`).
- It is the module's manual test script: only what cannot be checked from the code, written for a
  tester who has never seen it. Anything provable from the code belongs in a test instead.
- A step is written for the app rather than for one platform. Where the same check can be walked
  on iOS as on Android, it is one step phrased for both. Where only one platform can reach it, the
  step names that platform and says why.
- The `code-documentation` skill holds the rules for what goes in it and how far its scope reaches.
- A regression, of one module or of the whole app, is walked with the `manual-regression` skill.
  It also covers the regression's report and the triage of what it found.
- A regression **of one module** is run from that module's own `-REGRESS.md`, at the module's
  root.
- A regression **of the whole app** is run from `ANOTI-FULL-REGRESS.md` in the project root. It
  carries no steps of its own: it links to every module's regression file, and a full run means
  working through all of them.
- Creating or deleting a module's regression file includes adding or removing its link in
  `ANOTI-FULL-REGRESS.md`. A file that exists but is not linked is skipped silently by a full
  regression.
