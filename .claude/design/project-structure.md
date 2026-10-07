# Project structure

The repository's layout, the kinds of Gradle module it is built from, which kind may depend on
which, and how modules reach each other. What a single module looks like inside is in
[module-anatomy.md](module-anatomy.md); how to create one is in [new-module.md](new-module.md).

Read when: adding a module or choosing which kind it is; adding a dependency between modules;
deciding which module a new piece of code belongs to; finding your way around the repository.

## Top-level tree

```text
<root>/
├── androidApp/          Android host: the Android application module
├── iosApp/              iOS host: Xcode project generated from project.yml, Swift sources
├── main/                entry module
├── core-kmp/            core modules: composition root, DI-scope leaf, navigation,
│                        network, UI kit, persistence, test utilities
├── feature-kmp/         feature modules of every feature kind
├── config/              detekt and SwiftLint configuration
├── gradle/              version catalog, wrapper, daemon JVM properties
├── .github/             CI workflows, composite actions, scripts and their tests
├── .claude/             agent rules, skills and this design
├── build.gradle.kts     root build: shared plugin setup for every module
├── settings.gradle.kts  module list
├── gradle.properties
└── <APP>-FULL-REGRESS.md  full regression: links every module's regression file
```

`core-kmp` and `feature-kmp` are only path segments. Each module under them has its own build
file, and [settings.gradle.kts](../../settings.gradle.kts) includes it as `:core-kmp:<name>` or
`:feature-kmp:<name>`.

## Module kinds

| Kind | Role | Example |
|---|---|---|
| Android host | The Android application. Its one project dependency is the composition root, which exposes the entry module to it. It holds the `Application`, which builds the app graph and every root graph | [androidApp](../../androidApp) |
| iOS host | The Xcode app, not a Gradle module. It reaches Kotlin only through the framework | [iosApp](../../iosApp) |
| Composition root | Both app graphs (one per platform, same name), the one iOS framework, and the iOS entry object Swift calls | [core-kmp/di-app](../../core-kmp/di-app) |
| Entry module | The root graph, the root host, the root content, the platform screen hosts, the routes of root-level elements, and implementations of external contracts | [main](../../main) |
| Core: DI-scope leaf | Scope and qualifier annotations and the platform context; no component or binding | [core-kmp/di-scope](../../core-kmp/di-scope) |
| Core: navigation | The root component and its configs and deep links | [core-kmp/navigation](../../core-kmp/navigation) |
| Core: network | The HTTP client, its platform engines, and the safe-call wrapper | [core-kmp/network](../../core-kmp/network) |
| Core: UI kit | Common utilities and the UI kit: theme, tokens, shared composables, coroutine contexts, system messages, paging | [core-kmp/celebrity](../../core-kmp/celebrity) |
| Core: persistence | The database, its store and its exported usecases | [core-kmp/anime-database](../../core-kmp/anime-database) |
| Core: test utilities | Helpers for tests only; never a dependency of production code | [core-kmp/test-utils](../../core-kmp/test-utils) |
| Screen feature | One destination: screen component, controller, stores, source, UI | [feature-kmp/anime-list](../../feature-kmp/anime-list) |
| Root-level element | UI drawn over every screen: store and controller, no screen component; its route lives in the entry module | [feature-kmp/bottom-navigation-bar](../../feature-kmp/bottom-navigation-bar) |
| UI-only component | Composables and tokens only; its state holder lives in its host | [feature-kmp/notifications-rationale-dialog](../../feature-kmp/notifications-rationale-dialog) |
| Shared base module | What several features share: a service, response and domain models, shared composables | [feature-kmp/anime-base](../../feature-kmp/anime-base) |
| Platform service | A capability with a common contract and per-platform implementations | [feature-kmp/anime-notification](../../feature-kmp/anime-notification) |
| External module (`*-external`) | Contracts only. A feature calls them; the entry module implements them | [feature-kmp/anime-notification-external](../../feature-kmp/anime-notification-external) |

An external module exists so a feature can reach something the entry module owns without
depending on it. The notification module, for one, builds a tap payload through a contract the
entry module implements.
Example: [AnimeNotificationTapPayloadProvider](../../feature-kmp/anime-notification-external/src/commonMain/kotlin/com/alekseivinogradov/anoti/animenotification/external/kmp/api/presentation/provider/AnimeNotificationTapPayloadProvider.kt).

## Dependency edges

As found in the build files:

- Android host → composition root. Its tests add the modules they name.
- Composition root → the entry module and every module that contributes app-wide components.
- Entry module → screen features, root-level elements, UI-only components, shared base modules,
  platform services, external modules and core modules.
- Feature → shared base module, platform service, external module, core.
- Core → core, downward only: UI kit → network; persistence → UI kit; every core module
  with DI bindings → DI-scope leaf.
- The DI-scope leaf, navigation and the test utilities depend on no project.
- Nothing depends on the entry module except the composition root.
- No feature depends on another screen feature.

Example: [main build file](../../main/build.gradle.kts),
[anime-favorites build file](../../feature-kmp/anime-favorites/build.gradle.kts).

A dependency is `implementation` unless a public signature names the other module's types; see
[principles.md](principles.md), section "Small modules, explicit dependencies".

## Visibility

`api` and `impl` say what a declaration is (see [module-anatomy.md](module-anatomy.md), section
"Packages"). They do not limit who can read it: `impl` packages are public across modules, and
three kinds of reader rely on that:

- Features use the UI kit's composables, which sit in `impl` like every composable.
  Example: [AnotiTheme](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/AnotiTheme.kt).
- Features use a shared base module's composables.
  Example: [PullToRefreshBox](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/presentation/compose/PullToRefreshBox.kt).
- The entry module reads features' screen components, routes, controllers, composables and
  feature graphs.
  Example: [RootContent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/RootContent.kt).

Kotlin `internal` is what hides a declaration from other modules.

## How modules talk

- Injected `api` contracts. A feature takes another module's contract through its dependencies
  contract, and the root graph supplies it; see [dependency-injection.md](dependency-injection.md).
  Example: [DiAnimeListDependencies](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/di/DiAnimeListDependencies.kt).
- Store labels and controller binders. A controller binds one store's labels or state to another
  store's intents, including the persistence store; see [mvi.md](mvi.md), section "Controllers".
  Example: [AnimeListController](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/AnimeListController.kt).
- Shared base modules. Features that need the same service or models depend on one base module
  instead of on each other.
- External contracts. A feature declares what it needs from the entry module in an external
  module, and the entry module implements it and contributes the binding.
  Example: [AnimeNotificationTapPayloadProviderImpl](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/provider/AnimeNotificationTapPayloadProviderImpl.kt).
- Navigation. Screens do not open each other; the root component replaces the stack. See
  [navigation.md](navigation.md).
