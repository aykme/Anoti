The information dialog: links to the app's privacy policy and support pages, with an MVI store
of its own that a screen owns.

## Entities

- [InfoDialogStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/infodialog/kmp/api/domain/store/InfoDialogStore.kt) —
  the store. `State`/`Intent` are documented on the type itself.
- [InfoDialog](src/commonMain/kotlin/com/alekseivinogradov/anoti/infodialog/kmp/impl/presentation/compose/InfoDialog.kt) —
  the dialog. Tests outside this module find it by its `info_dialog` test tags.
- [InfoDialogConsts](src/commonMain/kotlin/com/alekseivinogradov/anoti/infodialog/kmp/api/domain/InfoDialogConsts.kt) —
  the two pages' addresses.
- [InfoDialogDimens](src/commonMain/kotlin/com/alekseivinogradov/anoti/infodialog/kmp/api/presentation/compose/InfoDialogDimens.kt) —
  the dialog's size tokens.

## How to include it

- Gradle: `api(project(":feature-kmp:info-dialog"))` in the screen's module, whose feature graph
  and controller name the store in their public signatures.
- The screen's feature graph mixes in `DiInfoDialogComponent` and declares an
  `abstract val infoDialogStore: InfoDialogStore`. Its parent supplies the `StoreFactory`, the
  `CoroutineContextProvider`, the `SystemMessageProvider` and
  [`web-page-opener`](../web-page-opener/FEATURE-KMP-WEB-PAGE-OPENER-README.md)'s
  `OpenWebPageUsecase`.
- The screen component reads the store once, beside its own stores, and disposes it with them.
  It saves `State.isShown` in its snapshot and replays `InfoClick` from its start hook when the
  dialog was open.
- `InfoDialog` has no DI wiring; the screen calls it from its own Compose content.

## How to use it

The screen's controller exposes the store's state and forwards its intents. The route collects
whether the dialog shows as a `State<Boolean>`, and the screen draws `InfoDialog` while it is
true. The screen's info button sends `InfoClick`, and the dialog's callbacks send
`PrivacyPolicyClick`, `SupportClick` and `CloseClick`. The store opens the pages itself. The
[anime list](../anime-list/FEATURE-KMP-ANIME-LIST-README.md) is the example.
