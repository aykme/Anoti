# Technical design

How code is written in this project, whatever its features do: how modules are built, which
entities exist and who owns what, how KMP, DI, navigation, saved state, UI, tests and docs work, and
how a new project is started in the same image. Feature code appears only as `Example:` links.

## Rules and design

- Rules say what to do and when; this design says how to write. The split is in
  [agent-setup.md "Rules and design"](agent-setup.md#rules-and-design), and how rules load in its
  [section "How rules load"](agent-setup.md#how-rules-load).
- A rule that sends you to a design file, and the task table below, are hard steps. Read the named
  files, in order, before writing. A list a design file opens with "Read" is a hard step too;
  every other link is a reference.
- A design file names a rule for what the rule owns, and never retells it.

## Placeholders

| Placeholder | Meaning | In this project |
|---|---|---|
| `<root-package>` | root package of every module | `com.alekseivinogradov.anoti` |
| `<App>`, `<APP>`, `<app>` | app name: type names, constants, lower case | `Anoti`, `ANOTI`, `anoti` |
| `<APP>_TAG` | the log tag | `ANOTI_TAG` |
| `<module-id>` | Gradle module name without hyphens | `animelist` for `anime-list` |
| `<Module>`, `<Feature>`, `<Name>` | a module, a feature, an entity in type names | `AnimeList` |
| framework | the one iOS framework | `Shared` |

## Tasks

Read the files of your row, in order, before the first write. A subagent sent to write or review
gets its row's files named in its prompt.

| Task | Read |
|---|---|
| Create a module | [new-module.md](new-module.md), [project-structure.md](project-structure.md), [module-anatomy.md](module-anatomy.md) |
| Add a screen | [recipes.md](recipes.md) "New screen feature", [navigation.md](navigation.md), [mvi.md](mvi.md) |
| Add a root-level element, overlay or dialog | [recipes.md](recipes.md) "Root-level element or overlay", [navigation.md](navigation.md) "Root-level elements", [ui-compose.md](ui-compose.md) |
| Add or change a store, executor, reducer or controller | [recipes.md](recipes.md) "New store in a screen", [mvi.md](mvi.md), [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md) |
| Add a network call | [recipes.md](recipes.md) "Network endpoint", [data-layer.md](data-layer.md) "Network", [error-handling.md](error-handling.md) |
| Add a paged list | [recipes.md](recipes.md) "Paged list", [data-layer.md](data-layer.md) "Paging", [mvi.md](mvi.md) |
| Change the database | [recipes.md](recipes.md) "Database change", [data-layer.md](data-layer.md) "Persistence", [testing-platforms.md](testing-platforms.md) |
| Add or change a DI binding | [recipes.md](recipes.md) "App-wide or root-scoped binding", [dependency-injection.md](dependency-injection.md), [entities.md](entities.md) |
| Add platform-specific code | [recipes.md](recipes.md) "Platform-specific implementation", [kmp.md](kmp.md) "Where code lives", [platform-mirroring.md](platform-mirroring.md) |
| Add a class of any kind, or name one | [entities.md](entities.md), [module-anatomy.md](module-anatomy.md) "Naming", [code-style.md](code-style.md) |
| Write Compose UI, a string, an icon or a constant | [ui-compose.md](ui-compose.md), [accessibility-and-adaptive-layout.md](accessibility-and-adaptive-layout.md), [recipes.md](recipes.md) "Shared constant, token or string" |
| Keep a screen's state across recreation | [state-restoration.md](state-restoration.md), [navigation.md](navigation.md) "Screen components" |
| Open a screen from a notification | [recipes.md](recipes.md) "Open a screen from a notification", [navigation.md](navigation.md) "Deep links", [platform-mirroring.md](platform-mirroring.md) |
| Add background work or a notification | [recipes.md](recipes.md) "Background work", "Notification", [platform-mirroring.md](platform-mirroring.md), [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md) |
| Show an error to the user | [error-handling.md](error-handling.md), [data-layer.md](data-layer.md) |
| Change Swift code or the iOS entry | [ios-host.md](ios-host.md), [platform-mirroring.md](platform-mirroring.md) |
| Read outside input, add a permission | [security-and-privacy.md](security-and-privacy.md) |
| Write a test or a test double | [testing.md](testing.md), [testing-platforms.md](testing-platforms.md) |
| Document a change | [documentation.md](documentation.md) |
| Add or bump a library | [recipes.md](recipes.md) "New library", [tech-stack.md](tech-stack.md), [decisions.md](decisions.md) |
| Bump a version, pick a build type | [versioning-and-release.md](versioning-and-release.md), [build-and-tooling.md](build-and-tooling.md) |
| Plan a task, commit, push or run CI | [development-workflow.md](development-workflow.md) |
| Start a new project | [new-project.md](new-project.md), [agent-setup.md](agent-setup.md), [project-structure.md](project-structure.md) |
| Extend the rules or this design | [agent-setup.md](agent-setup.md) |
| No pattern fits, or two conflict | [principles.md](principles.md), [decisions.md](decisions.md), [glossary.md](glossary.md) |

## Index

Cross-cutting:

- [principles.md](principles.md) — read when a task has no pattern to copy, or two patterns pull in
  different directions; before proposing a new kind of class, module or mechanism; when reviewing a
  design or a spec.
- [decisions.md](decisions.md) — read when about to propose a library, a tool or an approach that
  replaces or reverses one of these; when a review finding questions a choice listed here; before a
  version-bump or cleanup pass.
- [glossary.md](glossary.md) — read when a design file, a rule or a review uses a term you cannot
  place; naming a concept in a design file, a KDoc or a review, so it uses the same word as
  everything else.
- [entities.md](entities.md) — read when about to add a class, interface, object or file of a kind
  not written before in the task; deciding which kind a new piece of code is; checking what a kind
  may call; reviewing whether a new class fits a kind that already exists.
- [recipes.md](recipes.md) — read when a change spans several areas, as each heading below names
  one.

Structure:

- [project-structure.md](project-structure.md) — read when adding a module or choosing which kind it
  is; adding a dependency between modules; deciding which module a new piece of code belongs to;
  finding your way around the repository.
- [module-anatomy.md](module-anatomy.md) — read when creating a file, a package or a module; naming
  a class; choosing between `api` and `impl`; editing a manifest or a module's Compose resources
  settings.
- [new-module.md](new-module.md) — read when creating a Gradle module or choosing its kind; changing
  a module's build file or `settings.gradle.kts`.
- [tech-stack.md](tech-stack.md) — read when adding, removing or bumping a library or a plugin;
  editing `gradle/libs.versions.toml`; looking for the library that already does something.

Concepts:

- [kmp.md](kmp.md) — read when adding Kotlin code or deciding where it lives; adding
  platform-specific code; adding an `expect` declaration; changing a module's framework or
  Compose-compiler setup; adding Compose resources; writing code or tests that run on Kotlin/Native.
- [dependency-injection.md](dependency-injection.md) — read when adding or changing a DI binding, a
  `Di*Component`, a `Di*Dependencies` contract, a scope or a qualifier; adding a module that
  contributes bindings; building a graph in a host or a test.
- [navigation.md](navigation.md) — read when adding or changing a destination, a screen component or
  a route; adding a root-level element; changing how the root stack, the bar or a notification tap
  navigates.
- [state-restoration.md](state-restoration.md) — read when a screen must keep state across
  recreation or process death; changing what a screen saves or how it replays it; changing how a
  screen host or the iOS root holder keeps saved state.
- [mvi.md](mvi.md) — read when adding or changing a store, an executor, a reducer, a controller, or
  a mapper between stores or from a store's state to a UI model.
- [data-layer.md](data-layer.md) — read when adding or changing a network call, a service, a
  response model, a source, a usecase, the database or its schema, a model mapper at the data
  boundary, or a paged list.
- [error-handling.md](error-handling.md) — read when handling a failed call or page load, showing an
  error to the user, adding a coroutine scope or context, or catching an exception.
- [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md) — read when launching a coroutine or
  creating a scope; picking a context or dispatcher; binding stores to a lifecycle; adding a start
  or destroy hook; deciding who disposes a store.
- [ui-compose.md](ui-compose.md) — read when writing or changing a composable, a screen or its
  route, the theme, a design token (`Dimens`, `Fonts`, `Colors`) or a constants file, a Compose
  resource (string, drawable, font), a UI model's stability, or a preview.
- [accessibility-and-adaptive-layout.md](accessibility-and-adaptive-layout.md) — read when adding or
  changing a tappable control, an image or icon, a custom gesture or semantics, a layout that must
  hold at large text or display size, or anything that depends on the screen's size, its
  orientation, the system bars or the keyboard.
- [platform-mirroring.md](platform-mirroring.md) — read when adding or changing code that one
  platform has and the other must mirror; changing the `Application`, the screen host, the iOS entry
  object or anything they start; writing a `*Worker`, a background refresh, a notification or a
  permission request.
- [ios-host.md](ios-host.md) — read when changing Swift app code, `Info.plist`, or the `iosMain`
  code Swift calls.
- [security-and-privacy.md](security-and-privacy.md) — read when reading input from outside the app
  (an intent extra, a notification payload, a network response), adding a permission, a network host
  or a persisted field, writing a log line with data in it, or changing a manifest, the R8 rules or
  the iOS privacy manifest.
- [code-style.md](code-style.md) — read when writing Kotlin in any module, suppressing a detekt
  finding, or writing Swift or Markdown in the repository.

Process and tooling:

- [testing.md](testing.md) — read when writing or changing a test, a test double or a shared test
  utility; deciding which source set a test belongs in.
- [testing-platforms.md](testing-platforms.md) — read when writing an `androidHostTest`, a
  composable test or an `iosTest`; giving a module host tests; testing `iosMain` code.
- [documentation.md](documentation.md) — read when documenting a change; deciding where a piece of
  knowledge belongs; looking for a README, a regression file, a planning doc or this design.
- [build-and-tooling.md](build-and-tooling.md) — read when changing the root `build.gradle.kts`,
  `gradle.properties`, the daemon JVM settings, `config/` or a workflow; looking for the Gradle
  command that checks something.
- [versioning-and-release.md](versioning-and-release.md) — read when bumping the app version or a
  platform version; choosing a build type or configuration for a check; asking how a shipped build
  is signed.
- [development-workflow.md](development-workflow.md) — read when starting a task and planning its
  steps; before a commit, a push or a CI run; when finishing a task and checking that no step was
  skipped.
- [agent-setup.md](agent-setup.md) — read when adding or changing a rule, a skill or a design file;
  changing a rule's `paths:`; checking that links resolve and rules still reach their readers;
  repeating the reachability walk; looking for where a piece of agent knowledge lives.
- [new-project.md](new-project.md) — read when starting a new project in this image, by pointing
  Claude at this design or by copying it; carrying build, CI or agent files over to another
  repository.

## Other repositories

A new project in this image, or a read from another checkout, starts with
[new-project.md](new-project.md): path rules do not load there, so it lists what to read by hand.

## Maintenance

- A task that changes how something is built updates the design in the same task; a change to a
  file a skeleton names in its `Mirrors:` line updates that skeleton.
- A new design file follows
  [agent-setup.md "Adding a design file"](agent-setup.md#adding-a-design-file).
