---
name: creating-a-module
description: Use when a task in this project needs a new Gradle module - a new screen or bottom-bar tab, a root-level element or overlay, a UI-only component, a shared base, a platform service, an external contract module or a core library - or when new code has no fitting home in an existing module.
---

# Creating a module

Every module here is built one way, and the technical design owns that way. This skill orders the
work and ends with a script that proves the module has the shape its kind requires. It never
replaces the design: each step names the section that says how.

## Before the first file

Read with the Read tool, in this order: the files the task table of
`.claude/design/TECHNICAL-DESIGN.md` names for "Create a module", every file new-module.md's
section "Before the first file" names, then `.claude/rules/module-docs.md`. A screen feature
also reads `.claude/design/recipes.md` "New screen feature"; a root-level element or overlay,
"Root-level element or overlay"; a platform service, "Platform-specific implementation". Read
each area's design file before the first write in that area, not after.

## Steps

1. **Kind.** Walk new-module.md "Choose the kind" from the top; the first "yes" names it. State
   the kind and the question that answered "yes". If an existing module already does the job,
   extend it instead and stop here.
2. **Names.** new-module.md "Names": Gradle path, `<module-id>`, packages, namespace, `Res`
   package, README and regression file names.
3. **Settings.** One `include` line beside its group.
4. **Build file.** Skeleton (a), plus only what the kind needs from (b), (c) or (d) and the
   "Boilerplate notes".
5. **Sources.** `commonMain` first; another source set only when kmp.md "Where code lives"
   allows it. Packages follow module-anatomy.md "Packages".
6. **What the kind holds.** Every entity the kind table below lists; entities.md names the design
   file that says how each one is written. A screen with nothing to load yet still gets its
   store, feature graph and route.
7. **Wiring.** new-module.md "Wiring"; a screen also takes every step of navigation.md "Adding a
   destination".
8. **Tests.** testing.md, with its section "Test doubles", and testing-platforms.md.
9. **Docs.** The `code-documentation` skill writes the README and the regression file, and links
   the regression file from `ANOTI-FULL-REGRESS.md`.
10. **Check.** Run, from the repository root, until it prints no `FAIL` (`python3` where only
    that exists):
    `python -I .claude/skills/creating-a-module/scripts/check_module.py :<group>:<name> <kind>`
    Then finish with new-module.md "Checklist".

| Kind (script argument) | It holds                                                                                                                                                                                             |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `screen-feature`       | Dependencies contract, feature graph, main store (contract, executor, reducer, factory), controller, UI model and its mapper, screen, screen component, route, a root config named after the feature |
| `root-level-element`   | Store, controller, module component, composable; no screen component; its route lives in the entry module                                                                                            |
| `ui-only-component`    | Composables and tokens; no store, KSP or serialization                                                                                                                                               |
| `platform-service`     | A common contract in `api`, an implementation of the same name on each platform, a platform component on each                                                                                        |
| `shared-base`          | What several features share; nothing a kind above owns                                                                                                                                               |
| `external`             | Contracts only, common or Android-only; the entry module implements them                                                                                                                             |
| `core`                 | A library every layer may use                                                                                                                                                                        |

## Common mistakes

- A platform contract filed as a shared base because several features need it: it is a platform
  service.
- A screen without its store, feature graph or route, or a root config named unlike its screen
  component.
- A build file copied whole from a neighbor, with KSP, serialization or resources it does not use.
- An `api(...)` line without its reason comment.
- A regression file that `ANOTI-FULL-REGRESS.md` does not link.
- Code moved into the new module beyond what the task asked for.

On existing modules the script also reports the departures the design records as "As found". On
a new module, fix every `FAIL` the design backs; a `FAIL` that contradicts the design is a bug in
the script: report it, and never bend the module to silence it.
