# Accessibility and adaptive layout

How the UI stays usable for everyone and on every screen: text and display scale, touch targets,
semantics for screen readers, custom gestures, screen size and orientation, system bar insets and
the keyboard. The composables themselves are written as [ui-compose.md](ui-compose.md) says.

Read when: adding or changing a tappable control, an image or icon, a custom gesture or
semantics, or a layout that must hold at large text or display size. Also when adding or
changing anything that depends on the screen's size, its orientation, the system bars or the
keyboard.

## Text and display scale

- Text sizes are `sp` values from the `Fonts` tokens, so they follow the system font size.
- A text that can grow has a `maxLines` cap with `TextOverflow.Ellipsis`. The cap is a safety net
  for large scales; at normal scale the text never reaches it.
- A height that holds text is a minimum, not a fixed value (`heightIn(min = …)`), so it grows
  with the text. Example:
  [AnimeListTopBar](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListTopBar.kt).
- A width that would starve a neighbor at a large display size is a fraction of the row, not a
  fixed `dp`. Example:
  [AnimeFavoritesDimens.kt](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/presentation/compose/AnimeFavoritesDimens.kt).
- A row that may stop fitting wraps or shortens one chosen element, through a custom `Layout`;
  see [ui-compose.md](ui-compose.md), section "Layout".
- A non-breaking space inside a value keeps it whole, so a line wraps only where intended.
- Long dialog text scrolls inside the dialog, so its buttons stay in view. Example:
  [NotificationsRationaleDialog](../../feature-kmp/notifications-rationale-dialog/src/commonMain/kotlin/com/alekseivinogradov/anoti/notificationsrationaledialog/kmp/impl/presentation/compose/NotificationsRationaleDialog.kt).
- Manual checks run every screen at default and maximum font and display size, in both
  orientations. The pass matrix is in the "Passes" section of `<APP>-FULL-REGRESS.md`. Example:
  [ANOTI-FULL-REGRESS.md](../../ANOTI-FULL-REGRESS.md). How to walk it is in the
  [manual-regression skill](../skills/manual-regression/SKILL.md).

## Touch targets

- Material controls (`IconButton`, `FloatingActionButton`, `NavigationBarItem`, `TextButton`) keep
  their default minimum touch target.
- A tappable text answers on its whole padded area: the click modifier comes before the padding.
- As found: one control turns the Material minimum off to keep a smaller declared size. Example:
  [AnimeFavoritesItem](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/compose/AnimeFavoritesItem.kt).

## Semantics

- Every meaningful image and icon has a content description from a string resource. A decorative
  one passes `null`: a shadow copy of an icon, or an icon whose item already has a text label.
- A loading spinner takes a content description; `null` marks it as decorative. Example:
  [LoadingSpinner](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/LoadingSpinner.kt).
- A toggle's description names what a tap will do, and changes with its state ("turn on", "turn
  off").
- Tabs use `selectable(selected, role = Role.Tab)`, so a screen reader hears the role and which
  tab is open; color alone conveys neither. A tab drawn as an icon button sets the same role and
  `selected` through `semantics`. Example:
  [AnimeListTopBar](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListTopBar.kt).
- A lazy grid states its real size with `collectionInfo`; by default it announces an unknown size.
  Example:
  [AnimeListScreen](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListScreen.kt).
- Information a component drops from semantics moves to an element that keeps it. A navigation
  item with a label drops its icon's semantics, so the label's description carries the badge
  count. Example:
  [BottomNavigationBar](../../feature-kmp/bottom-navigation-bar/src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/presentation/compose/BottomNavigationBar.kt).
- Content composed only to be measured is hidden with `clearAndSetSemantics {}`, so a screen
  reader never reads it twice. Example:
  [PosterInfoRow](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/compose/PosterInfoRow.kt).

## Custom gestures

- A custom gesture modifier also declares its semantics: a `Role` and an `onClick` action. A
  screen reader's activation then calls the action once, without the gesture. Example:
  [RepeatingClickable](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/RepeatingClickable.kt).
- Repeat-on-hold stays touch-only; each repeat is the same action a single tap performs.
- A nested gesture consumes its pointer events, so an ancestor's click or long-press does not fire
  with it.
- A long-press on an item is a shortcut for an action a visible control on the item also offers.
  Example:
  [AnimeFavoritesItem](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/compose/AnimeFavoritesItem.kt).

## Screen size and orientation

- One threshold decides rotation on both platforms. A screen whose smaller side is under 600 keeps
  the app upright; from 600 on, the app turns with the device. The shared check is
  [WindowRotation.kt](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/orientation/WindowRotation.kt).
- Android measures the whole display in `dp` at the density the device ships with, so the display
  size setting never locks a tablet upright. The activity sets its requested orientation at
  creation and again on every configuration change. Example:
  [DeviceOrientation.kt](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/orientation/DeviceOrientation.kt).
- iOS measures the window in points and re-checks when the window's size changes. The Swift side
  of this is in [ios-host.md](ios-host.md).
- A layout adapts to the room it really has, not to the window. A grid reads its own width for
  its column count: one column under 600 `dp`, two from it on. Example:
  [AnimeListGridCells](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/compose/AnimeListGridCells.kt).
- The grid compares widths in pixels, since a width converted to `dp` can land just under a
  threshold it meets.
- On Android a rotation, a resize, a density change and a fold keep the activity alive. Which
  changes do so, and what that means for state, is in
  [state-restoration.md](state-restoration.md), section "Scenarios".

## Insets

- Both platforms draw edge to edge, and the Compose content pads itself. Android calls
  `enableEdgeToEdge` with transparent dark bars. On iOS the hosting view ignores every safe area;
  see [ios-host.md](ios-host.md).
- The UI kit module has the inset helpers: `Modifier.horizontalSystemBarsPadding()` and
  `systemBarsTopPadding()`. Example:
  [SystemBarsInsets.kt](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/SystemBarsInsets.kt).
- A screen pads its root horizontally for the system bars, so a side navigation bar in landscape
  never covers it.
- A list takes the top inset as its content padding, so it scrolls under the status bar and
  starts below it.
- The bottom bar fixes its own content height and reserves the navigation bar's inset with a
  separate spacer in its color. Only one layer pads for that inset.

## Keyboard

- Android declares `adjustResize` on the activity. Without it, API 29 reports no keyboard insets.
  See
  [main/src/androidMain/AndroidManifest.xml](../../main/src/androidMain/AndroidManifest.xml).
- iOS sets `onFocusBehavior = OnFocusBehavior.DoNothing` on the Compose view controller. Compose
  then sees the keyboard as insets instead of panning the whole view. Example:
  [IosScreenHost.kt](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/api/presentation/IosScreenHost.kt).
- Content that must stay above the keyboard reads `WindowInsets.ime` in the layout phase. The
  system message host sits above whichever is higher, the bottom bar or the keyboard. Example:
  [RootContent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/RootContent.kt).
- When a text field closes, the screen hides the keyboard through
  `LocalSoftwareKeyboardController`.
