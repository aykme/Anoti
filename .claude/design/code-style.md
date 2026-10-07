# Code style

How Kotlin is written across all modules: formatting, static analysis, files and declarations,
types, naming, calls and suppressions. It also points to the comment, logging, Swift and Markdown
conventions. Module layout, packages and class-name suffixes are in
[module-anatomy.md](module-anatomy.md).

Read when: writing Kotlin in any module, suppressing a detekt finding, or writing Swift or
Markdown in the repository.

## Formatting and static analysis

- Kotlin follows the official style: `kotlin.code.style=official` in
  [gradle.properties](../../gradle.properties).
- Indents, line endings and the 100-column limit come from [.editorconfig](../../.editorconfig),
  as [build-and-tooling.md "Gradle settings"](build-and-tooling.md#gradle-settings) lists.
- detekt runs its default rules plus [config/detekt/detekt.yml](../../config/detekt/detekt.yml).
  The config turns on extra coroutine, performance, potential-bug and style rules, and turns off
  two with the reason beside each.
- The detekt plugins, its type resolution and how it is wired into the build are in
  [build-and-tooling.md](build-and-tooling.md).

## Files and declarations

- A file holds one main top-level type and is named after it. Private helpers only that type
  uses stay in its file. A small data holder tied to the main type may share it. Example:
  [NavAnimeListScreenComponent.kt](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponent.kt).
- A file of top-level functions or values is named for what it holds: `<Module>Dimens.kt`,
  `Typealias.kt`. Mapper files are named as [mvi.md](mvi.md) "Mappers and UI models" says.
- A file that renders the parts of one composable may grow past detekt's function count. It
  carries `@file:Suppress("TooManyFunctions")` with the reason above it.

## Visibility

- Most declarations, implementations in `impl` packages included, are public.
- `internal` marks types only their own module uses. Example: the reducer implementations, such
  as
  [AnimeFavoritesReducerImpl.kt](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/domain/store/AnimeFavoritesReducerImpl.kt).
- `private` keeps helpers to their file or class.
- Why the iOS app graph stays `internal` is in [ios-host.md](ios-host.md).

## Immutability

- Properties are `val` and collections read-only. State changes produce a new value with `copy`.
- A `var` stays local or private where it can: a counter, an accumulator, a job handle. An
  executor's application state is never such a `var`; see [mvi.md](mvi.md), section "Executors
  and state".
- UI models hold immutable collections; see [ui-compose.md](ui-compose.md), section "Stability".

## Types

- A closed set with payloads is a sealed type, mostly a sealed interface, whose members are
  `data class` or `data object`: store intents, labels and messages, page results. Example:
  [AnimeFavoritesMainStore.kt](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/domain/store/AnimeFavoritesMainStore.kt).
- Root configs are a sealed interface of `data object`s with no payload; see
  [navigation.md "Root stack"](navigation.md#root-stack).
- A closed set without payloads is an `enum class`, as UI flags such as content types are.
- A `when` over a sealed type or an enum lists every case and has no `else`. detekt's
  `ElseCaseInsteadOfExhaustiveWhen` is on for this, but it reports only where detekt resolves
  types (see [build-and-tooling.md](build-and-tooling.md#root-build)); in `commonMain` code
  review enforces it. `else` stays for open subjects, such as a
  string or a `Throwable`, and for a `when` without a subject.
- As found: three section executors end a `when` over a sealed result with `else -> Unit`.
  Example:
  [OngoingSectionExecutorImpl.kt](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/ongoingsection/OngoingSectionExecutorImpl.kt).
- A typealias gives a primitive its domain meaning (an id or an index over `Int`), or shortens a
  long generic base type. Example:
  [Typealias.kt](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/Typealias.kt).
  Executor typealiases are in [mvi.md](mvi.md).

## Naming

- Class-name suffixes and packages are in [module-anatomy.md](module-anatomy.md), section
  "Naming".
- Mapper names: store and UI mappers in
  [mvi.md "Mappers and UI models"](mvi.md#mappers-and-ui-models); boundary mappers are
  extensions `to<Target>()`, in [data-layer.md](data-layer.md).
- Constants are SCREAMING_SNAKE_CASE and end in their unit where they have one: `_DP`, `_SP`,
  `_PERCENT`, `_MILLIS`, `_SECONDS`, `_MINUTES`. As found: `_MILLISECONDS` in two constants files
  and `_MS` in one composable file. Example:
  [AnimeBaseDimens.kt](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/presentation/compose/AnimeBaseDimens.kt).
  Palette colors are PascalCase (`Cinnabar500`).
- A Boolean reads as a statement: `is*`, `has*`, `can*`.

## Calls and lambdas

- A call with several arguments usually names them, one per line once it wraps.
- A Java or platform call cannot take named arguments. Each argument gets a `/* name = */`
  comment on its own line above it. Example:
  [MainActivity.kt](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt).
- A named lambda parameter often states its type: `{ granted: Boolean -> … }`. A short one-line
  lambda uses `it`.

## Suppressions

- An `@Suppress` names the detekt rule and carries a one-line reason. The reason sits in a comment
  right above it, or inside the suppressed element. Example:
  [AnotiTheme.kt](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/AnotiTheme.kt).
- The composable `FunctionNaming` pattern is in [ui-compose.md](ui-compose.md), section
  "Composable naming".
- Why `RedundantSuspendModifier` is off in the config is in [decisions.md](decisions.md).

## Comments and logging

- Comments and KDoc follow [code-comments.md](../rules/code-comments.md).
- Log lines follow [logging.md](../rules/logging.md).

## Swift

- Swift is checked by SwiftLint with
  [config/swiftlint/swiftlint.yml](../../config/swiftlint/swiftlint.yml). When and how it runs is
  in [swiftlint.md](../rules/ios/swiftlint.md).

## Markdown

- Prose wraps at 100 columns. A line holding only a long link may run past it.
- Spelling is American: color, behavior, afterward.
- Headings are ATX (`#`); bullets use `-`.
- The root `README.md` is the GitHub page and follows none of this; see
  [CLAUDE.md](../../CLAUDE.md).
