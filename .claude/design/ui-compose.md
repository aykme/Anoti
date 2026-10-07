# Compose UI

How the UI is written: Compose Multiplatform composables in `commonMain`, the theme, the design
tokens, Compose resources, stability, lists, layouts, images and previews. Where a composable's
source set is decided is in [kmp.md](kmp.md), section "Where code lives".

Read when: writing or changing a composable, a screen or its route, the theme, a design token
(`Dimens`, `Fonts`, `Colors`) or a constants file, a Compose resource (string, drawable, font), a
UI model's stability, or a preview.

## Screens and routes

- All UI is Compose Multiplatform in `commonMain`. The Android activity and the iOS view
  controller show the root content and draw nothing of their own.
- A screen composable is stateless. It takes the screen's UI model, a `dispatch` function for the
  store's intents, and `modifier: Modifier = Modifier` as its first optional parameter. Extra
  inputs it cannot render without, such as a formatter, come in as parameters too.
- The screen switches on the UI model's content type. Each branch is a private composable in the
  same file (loading, empty or error, loaded).
- User events reach the store as intents through `dispatch`. Decisions about data stay in the
  store.
- An effect that outlives one `dispatch` value reads it through `rememberUpdatedState`. An effect
  is keyed on the value it reacts to. Example:
  [AnimeListScreen](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListScreen.kt).
- UI state that must outlive a content-type switch, such as a list's scroll state, is remembered
  above the switch. What survives a restart is in
  [state-restoration.md](state-restoration.md).
- A `testTag` id is snake_case.

```kotlin
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun <Feature>Screen(
    uiModel: <Feature>UiModel,
    // inputs it cannot render without, such as a formatter
    dispatch: (<Feature>MainStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    // an effect that outlives one dispatch reads it through rememberUpdatedState
    Box(modifier.fillMaxSize().horizontalSystemBarsPadding()) {
        when (uiModel.contentType) {
            ContentTypeUi.LOADING -> LoadingState()
            ContentTypeUi.EMPTY -> EmptyState()
            ContentTypeUi.LOADED -> ListState(uiModel = uiModel, dispatch = dispatch)
        }
    }
}
```

Mirrors: [AnimeFavoritesScreen](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/compose/AnimeFavoritesScreen.kt)

- A route connects a screen component to its screen. It collects the controller's state with
  `collectAsState()`, maps it in `remember(state) { mapStateToUiModel(state) }`, and passes
  `controller::accept` as `dispatch`. Example:
  [AnimeFavoritesRoute](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/AnimeFavoritesRoute.kt).

```kotlin
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun <Feature>Route(screenComponent: Nav<Feature>ScreenComponent) {
    val controller = screenComponent.controller
    val state by controller.state.collectAsState()
    val uiModel = remember(state) { mapStateToUiModel(state) }
    <Feature>Screen(uiModel = uiModel, dispatch = controller::accept)
}
```

Mirrors: [AnimeFavoritesRoute](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/AnimeFavoritesRoute.kt)

- Where a route lives and how the root content picks it are in [navigation.md](navigation.md).
  The controller and the state-to-UI-model mapper are in [mvi.md](mvi.md).

## Theme

- The UI kit module owns the theme composable: `MaterialTheme` with the app's color scheme over a
  full-size `Surface`. The `Surface` sets the content color, so a plain `Text` is readable.
  Example:
  [AnotiTheme](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/AnotiTheme.kt).
- The root content wraps the whole tree in the theme once. Screens do not wrap themselves;
  previews do.
- The scheme follows `isSystemInDarkTheme()`, with a light and a dark scheme. As found: the light
  scheme aliases the dark one, so the app is dark in both modes.
- Content that must not fill its window takes the color-scheme function instead of the wrapper.
  As found: only the theme itself calls it today.
- Before the first frame, the platform paints the window: the Android window theme in the UI
  kit's `androidMain/res`, and the launch screen color on iOS. Both match the theme background.

## Design tokens

- Shared Compose UI constants live in typed files by kind: `Dimens.kt` (sizes, spacing,
  corner/alpha percentages — `Dp`/`Int`), `Fonts.kt` (text sizes — `TextUnit`), `Colors.kt` (the
  color palette). `Const.kt` is separate and holds only business-logic constants (paging, timing,
  domain limits), never UI values. The one exception is the log tag `<APP>_TAG`, kept in the
  UI kit module's `<Module>Consts.kt` (Example:
  [CelebrityConsts.kt](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/CelebrityConsts.kt)).
- Placement: a constant used by more than one module lives in the closest common dependency every
  consumer already has (e.g. the UI kit module for project-wide values, a shared base module for
  values shared only among the feature screens that already depend on it; Example:
  [Colors.kt](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/presentation/compose/Colors.kt),
  [AnimeBaseDimens.kt](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/api/presentation/compose/AnimeBaseDimens.kt)).
  A constant used by exactly one module lives in a local file of the same kind in that module's
  own package. Don't leave it as a bare `private const val` at the bottom of a UI/Composable file.
- When more than one module has its own local `Dimens.kt`/`Fonts.kt`, prefix the file name with
  the module name (e.g. `<Module>Dimens.kt`; Example:
  [AnimeListDimens.kt](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/presentation/compose/AnimeListDimens.kt),
  [AnimeFavoritesDimens.kt](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/compose/AnimeFavoritesDimens.kt)).
  An import or search then says unambiguously which one it means.
- Comments on these constants describe what the value represents, not which features or screens
  consume it. That list changes independently of the value and shouldn't be hardcoded.

As found: the business-constant files are named `<Module>Consts.kt`, not `Const.kt`. Example:
[AnimeListConsts.kt](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/api/domain/AnimeListConsts.kt).

## Resources and localization

- Each module keeps its own Compose resources. Their directories and the `Res` class settings are
  in [module-anatomy.md](module-anatomy.md), sections "Source sets" and "Manifests and resources".
- A string shared by several features lives in their shared base module; a project-wide one in
  the UI kit module. A file using several modules' `Res` imports them under aliases
  (`Res as BaseRes`).
- A composable reads text with `stringResource`. Code outside composition passes a
  `StringResource` along and resolves it with the suspending `getString` where it is shown.
  Example:
  [SystemMessageHost](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/SystemMessageHost.kt).
- Content descriptions are string resources named `*_description`. As found: two descriptions
  reuse a message string (`loading_in_progress`, `connection_error`), and one is spelled
  `_discription`. Most drawables end in their size in dp (`ic_search_32`, `connection_error_48`).
- The app has one locale: only `values/` exists. Why is in [decisions.md](decisions.md).
- Simple icons are drawn white (`#FFFFFF`) and tinted in code: `ColorFilter.tint`, an `Icon`'s
  `tint`, or a component's content color. One drawable then serves every color.
- Platform resources: [kmp.md "Resources"](kmp.md#resources).

## Stability

- UI models are data classes of `val`s. A list field is an `ImmutableList` defaulting to
  `persistentListOf()`, and a model holding one carries `@Immutable`. Example:
  [AnimeFavoritesUiModel](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/model/AnimeFavoritesUiModel.kt).
- A model of plain values needs no annotation. Example:
  [BottomNavigationBarUiModel](../../feature-kmp/bottom-navigation-bar/src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/api/presentation/model/BottomNavigationBarUiModel.kt).
- The compiler-report check, its pass criteria, when to annotate, and where Compose annotations
  may not go are in [compose-compiler-reports.md](../rules/compose-compiler-reports.md).

## Recomposition

- A frequently changing value is read as late as possible. Scroll position is read through
  `snapshotFlow` in an effect, and a value derived from it through `derivedStateOf`.
- A value only layout needs is read in the layout phase, in a `layout` modifier.
- An overlay that toggles reads its `State` inside its own small composable, so only the overlay
  recomposes. Example:
  [RootContent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/RootContent.kt).

## Lists

- Lazy items are keyed by a stable id. When one lazy list can show different logical lists with
  overlapping ids, the key is scoped by the source: `"${section}_${id}"`. Example:
  [AnimeListScreen](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListScreen.kt).
- `animateItem()` is left out where a switch replaces the whole item set.
- Pull-to-refresh uses the shared base module's wrapper, also around an error state.

## Layout

- `Row`/`Column` `weight()` splits space by fraction, not by need. Where a child should keep its
  natural size until it no longer fits, a custom `Layout` measures it first, through intrinsics,
  and only then constrains it. Example:
  [AnimeListItem](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListItem.kt).
- Where a child cannot answer intrinsic queries, a `SubcomposeLayout` measures a copy instead,
  hidden from semantics. Example:
  [PosterInfoRow](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/compose/PosterInfoRow.kt).
- Slots of a custom `Layout` are found by `layoutId` from a private enum.

## Images

- Remote images load through Coil 3. `SubcomposeAsyncImage` is used where the loading slot is an
  animated composable. The `coil-network-ktor3` dependency gives the default loader its Ktor
  fetcher.
- Loading and error slots carry the same content description as the image.

## Previews

- Previews are private composables at the end of the file, named `<Name><Variant>Preview`,
  wrapped in the theme, with `@Suppress("FunctionNaming", "UnusedPrivateMember")`.
- A component smaller than a screen sets `widthDp` and `heightDp` from preview constants in its
  module's `Dimens` file. Without both, the theme's full-size `Surface` fills a whole device.
- A preview reaching a remote image provides `LocalAsyncImagePreviewHandler` with a local
  painter. Example:
  [AnimeListItem](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListItem.kt).

## Composable naming

- detekt's `FunctionNaming` expects lowerCamelCase, and composables use PascalCase. Each
  composable carries `@Suppress("FunctionNaming")`. The first one in a file has the reason comment
  shown in the skeleton above.
- A composable that returns a value, such as a modifier or a color scheme, is lowerCamelCase and
  needs no suppression.
