# State restoration

What survives what on each platform, how Android and iOS keep the root's saved state, and the
screen restore protocol every screen component follows. The platform owns lifecycle and saved
state; shared code only registers what it needs and reads it back.

Read when: a screen must keep state across recreation or process death; changing what a screen
saves or how it replays it; changing how a screen host or the iOS root holder keeps saved state.

## Scenarios

| Scenario                                                                                                  | Platform | What happens                                                                                       | What comes back                                                      |
|-----------------------------------------------------------------------------------------------------------|----------|----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| Density, orientation, screen size, screen layout or smallest width changes                                | Android  | Declared in `configChanges`; the activity stays and `onConfigurationChanged` runs                  | Everything; nothing is rebuilt                                       |
| Any other configuration change (locale, `uiMode`, `fontScale`, keyboard, `layoutDirection`, and the rest) | Android  | The activity is recreated in the same process: a new root graph and root host over the saved state | The active screen, its saved parts and its `rememberSaveable` values |
| The system ends the process in the background                                                             | Android  | A new process creates the activity over the saved state                                            | As above                                                             |
| The user force-stops the app                                                                              | Android  | A fresh start                                                                                      | Nothing                                                              |
| The scene leaves `.active`                                                                                | iOS      | Swift stores the root holder's string in `@SceneStorage`                                           | Nothing yet                                                          |
| The system ends the app in the background                                                                 | iOS      | The next launch builds the root over the kept string                                               | As on Android, when the app version matches                          |
| A crash, a force quit or a swipe away                                                                     | iOS      | iOS drops the kept string                                                                          | Nothing                                                              |
| A switch to another screen                                                                                | Both     | The stack replaces the screen; its screen component is destroyed and its stores disposed           | Nothing of the old screen; it opens fresh next time                  |
| A notification tap while the root lives                                                                   | Both     | The live root navigates; see [navigation.md](navigation.md) "Deep links"                           | The current screen, when it is the target                            |
| A notification tap that starts the app with nothing kept                                                  | Both     | The root opens on the target over no saved state                                                   | Nothing                                                              |
| A notification tap into a root rebuilt from saved state                                                   | Both     | The saved state is restored, then the root navigates                                               | The restored root, on the target                                     |

- The handled changes are listed in the entry module's
  [AndroidManifest.xml](../../main/src/androidMain/AndroidManifest.xml). The screens draw no
  qualified resources, so those changes have nothing to swap.
- Process-wide values outside saved state follow the process, not the root, such as the
  notification permission session the Android `Application` and the iOS root holder keep. A root
  rebuilt in the same process finds them as they were; a new process starts them again.
- Covered by tests. Example:
  [MainActivityRecreationTest](../../main/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivityRecreationTest.kt).

## Android

- The activity builds the root host after `super.onCreate`, because `defaultComponentContext()`
  reads the `SavedStateRegistry` only after it is restored. Example:
  [MainActivity](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt).
- The root host asks for its context once, through `createComponentContext(discardSavedState)`.
  A root opening on a target asks for a context without saved state. Example:
  [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt).
- A fresh activity reads the launching intent's target. An activity rebuilt over saved state
  ignores it and restores the screen the user was on.
- `rememberSaveable` goes through the activity's own saved state. Root content's `Children` keeps
  each screen's values under that screen's key and drops them once the screen leaves the stack.

## iOS

- iOS has no saved-instance state for the shared code. The root holder writes the root's state and
  the screen's `rememberSaveable` values into one string. Example:
  [IosRootHolder](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/IosRootHolder.kt).
- How Swift reads and stores the string is in [ios-host.md](ios-host.md).
- The root is built on the first request, over the kept string, and lives as long as the app. A
  later request gets the same root and ignores its string.
- The string is one JSON object with three parts:
  - `appVersion`: the build that wrote it. A string from another build is dropped.
  - `stateKeeper`: the root's StateKeeper container. It carries the stack and every screen's
    registered parts, any `@Serializable` type.
  - `saveable`: the `rememberSaveable` values, written by the saveable-state codec.
- An empty string, one that does not parse, or one from another build gives a fresh root, with a
  log line.
- A screen that rejects its saved part throws while the root is built. The holder then closes that
  attempt and builds a fresh root. Each attempt runs on its own child of the app's lifecycle, so
  destroying it closes the attempt's stores. Example:
  [ChildLifecycle](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/lifecycle/ChildLifecycle.kt).
- Saving returns `null` before the root exists, so the scene keeps what it has. A save that fails
  returns an empty string, so no older state comes back.
- The codec carries `null`, `String`, `Boolean`, `Int`, `Long`, `Float`, `Double`, `List`, `Map`,
  and snapshot mutable states, the specialized number states included. A state keeps its policy:
  structural, referential or never-equal. Each value carries a type tag, so it comes back as the
  same type. Example:
  [SaveableStateCodec](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/savedstate/SaveableStateCodec.kt).
- The codec refuses snapshot lists and maps, which would come back as plain ones. A value it cannot
  carry drops its whole key, with a log line. Nothing in it throws.
- Every composition of the root gets its own saveable-state registry, seeded from the live one or
  the last one. The registry accepts any value; the codec drops what it cannot carry. Example:
  [IosRootContent](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/IosRootContent.kt).
- The registry is attached after the content, since Compose forgets in reverse order. Its values
  are kept when the composition ends, before the screen's entries leave it.

## Screen restore protocol

A screen component reads and writes its saved state in this order:

1. Register disposal of its stores with `doOnDestroy`, before reading saved state. A saved state
   the screen rejects throws while it is read, and the stores still close with the lifecycle.
2. Call `stateKeeper.consume` for its key, then `stateKeeper.register` for the same key. Consume
   happens once, at construction.
3. Subscribe a one-time start hook, `doOnStart(isOneTime = true)`, that replays what was consumed.
   It is subscribed before the controller is built, so the replay comes before the controller's
   binder starts.

The saved part takes one of two shapes:

- A snapshot: a `@Serializable` data class of what the screen needs back. The start hook passes it
  to an `applyRestored<Name>State` function, which sends intents straight to the stores. Labels
  would reach no store yet, since the binder has not started. On a fresh start the same function
  receives `null` and applies the defaults. Example:
  [NavAnimeListScreenComponent](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponent.kt).
- A marker: a `@Serializable data object`. A non-null consume means the screen was rebuilt while
  it was active, and the start hook skips what only a fresh arrival does. Example:
  [NavAnimeFavoritesScreenComponent](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/NavAnimeFavoritesScreenComponent.kt).

The skeleton is in [navigation.md](navigation.md) "Screen components".

Store bootstrappers fit this protocol:

- A screen store gets an empty `SimpleBootstrapper()`. Nothing loads when it is created; the start
  hook opens the screen. Example:
  [AnimeFavoritesMainStoreFactory](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/domain/store/AnimeFavoritesMainStoreFactory.kt).
- A store that subscribes itself to its source gets a bootstrapper with that action. Example:
  [AnimeDatabaseStoreFactory](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/domain/store/AnimeDatabaseStoreFactory.kt).
- As found: the root-level element's store has no bootstrapper. Example:
  [BottomNavigationBarStoreFactory](../../feature-kmp/bottom-navigation-bar/src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/domain/store/BottomNavigationBarStoreFactory.kt).

## What is never saved

- Data the database or the network owns. A screen saves how much it had loaded and its display
  state; the list itself is fetched or read again.
- As found: one snapshot also saves per-item details fetched from the network after the list, so
  a restore does not fetch them again. Example:
  [NavAnimeListScreenComponent](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponent.kt).
- A list's scroll position goes through `rememberSaveable`, not the screen's saved part.
- Saved state can hold user input, so it is never logged; see [logging.md](../rules/logging.md).

## Adding saved state

1. Decide what the screen needs back that no store, database or network gives it again.
2. Pick the shape: a snapshot for values, a marker for "this screen was rebuilt".
3. Declare the type as `@Serializable internal` in the screen component's file, with a private key
   constant.
4. Follow the screen restore protocol above.
5. For a snapshot, write `applyRestored<Name>State` so it also handles a fresh start.
6. For UI-only values, use `rememberSaveable` with types the iOS codec carries.
7. Cover the replay and the component. Examples:
   [ApplyRestoredMainStateTest](../../feature-kmp/anime-list/src/commonTest/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/ApplyRestoredMainStateTest.kt),
   [NavAnimeFavoritesScreenComponentTest](../../feature-kmp/anime-favorites/src/commonTest/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/NavAnimeFavoritesScreenComponentTest.kt).

## Tests

- How tests are written is in [testing.md](testing.md); host and iOS specifics are in
  [testing-platforms.md](testing-platforms.md).
- The root's restore paths are covered in
  [RootHostTest](../../main/src/commonTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHostTest.kt),
  the iOS string in
  [IosRootHolderTest](../../main/src/iosTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/IosRootHolderTest.kt)
  and the codec in
  [SaveableStateCodecTest](../../main/src/iosTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/savedstate/SaveableStateCodecTest.kt).
- On CI the iOS app is also checked for restore on a simulator by
  [ios-restore-checks.sh](../../.github/scripts/ios-restore-checks.sh), which finds some log lines
  by their wording.
