# Navigation

How the app moves between screens: the root stack and its configs, the root host that builds each
root child, the screen component that owns a screen's graph and stores, the route that draws it,
root-level elements that live beside the stack, and how a notification opens a screen. The screen
restore protocol is owned by [state-restoration.md](state-restoration.md).

Read when: adding or changing a destination, a screen component or a route; adding a root-level
element; changing how the root stack, the bar or a notification tap navigates.

## Root stack

- Navigation is Decompose. The navigation core module holds the root component and the root
  configs, and depends on no other project module. Example:
  [NavRootComponent](../../core-kmp/navigation/src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootComponent.kt).
- The root configs form one `@Serializable` sealed interface. Each destination is a `data object`
  with its own `@SerialName`. Saved state and notification payloads carry that name, so it stays
  stable once shipped.
- A config, its `@SerialName` and its screen component share one name, the feature's: `AnimeList`
  and `NavAnimeListScreenComponent`.
- The root component wraps `childStack` with `handleBackButton = true`. Its only navigation call
  replaces the whole stack, so the stack always holds exactly one child. Navigating to the screen
  already shown keeps it; any other target creates the new child and destroys the old one.
- With one entry there is no back stack: on Android, system back leaves the app.
- The root component knows nothing about what a child is. The caller passes the child type and a
  child factory, so the core module stays free of feature modules.
- The root host in the entry module builds the root component. Its child factory maps each config
  to a root child. A root child wraps one screen component and names the bar's section for it.
  That mapping lives in one place, so no caller maps a screen to a section by hand. Example:
  [NavRootChild](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/navigation/NavRootChild.kt).
- The child factory builds a fresh feature graph for every child it creates. Example:
  [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt).
- Root content shows the stack with `Children` and switches on the root child to call its route.
  `Children` keeps each child's `rememberSaveable` values under that child's key. Example:
  [RootContent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/RootContent.kt).
- As found: the root component defaults its start config to one destination, and the root host
  passes the same default again.

```kotlin
@Serializable
sealed interface NavRootConfig {

    @Serializable
    @SerialName("<Feature>")
    data object <Feature> : NavRootConfig
}
```

Mirrors: [NavRootConfig](../../core-kmp/navigation/src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootConfig.kt)

## Screen components

- A screen component is `Nav<Feature>ScreenComponent` in the feature's
  `impl/presentation/navigation` package. It takes its `ComponentContext` and its feature graph,
  and delegates `ComponentContext` to the context. The feature graph is built in
  [dependency-injection.md](dependency-injection.md) "Component hierarchy".
- It reads each store from the feature graph once and disposes it as
  [concurrency-and-lifecycle.md "Disposal"](concurrency-and-lifecycle.md#disposal) says.
- It builds the screen's controller once, over its own lifecycle. The controller lives as long as
  the component. Controllers are owned by [mvi.md](mvi.md) "Controllers".
- Its lifecycle starts when its config becomes active and ends when the stack replaces it.
- It exposes only what the route needs: the controller, and values such as a formatter. Stores
  stay `internal`.
- The order of disposal, saved-state reading and the one-time start hook is the screen restore
  protocol in [state-restoration.md](state-restoration.md) "Screen restore protocol".
- A route draws the screen component. It lives next to the controller's owner, so a screen route
  sits beside its screen component. How it is written is in
  [ui-compose.md "Screens and routes"](ui-compose.md#screens-and-routes).

```kotlin
class Nav<Feature>ScreenComponent(
    componentContext: ComponentContext,
    di<Feature>Component: Di<Feature>Component
) : ComponentContext by componentContext {

    internal val mainStore: <Feature>MainStore = di<Feature>Component.mainStore

    init {
        lifecycle.doOnDestroy { mainStore.dispose() }
    }

    private val restoredState: Restored<Name>State? =
        stateKeeper.consume(key = RESTORED_STATE_KEY, strategy = Restored<Name>State.serializer())

    init {
        stateKeeper.register(key = RESTORED_STATE_KEY, strategy = Restored<Name>State.serializer()) {
            Restored<Name>State(/* read from mainStore.state */)
        }
        lifecycle.doOnStart(isOneTime = true) {
            applyRestored<Name>State(restoredState = restoredState, mainStore = mainStore)
        }
    }

    val controller = <Feature>Controller(lifecycle = lifecycle, mainStore = mainStore)

    private companion object {
        private const val RESTORED_STATE_KEY = "<Feature>MainStoreRestoredState"
    }
}
```

Mirrors: [NavAnimeListScreenComponent](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/presentation/navigation/NavAnimeListScreenComponent.kt)

## Root-level elements

- A root-level element sits beside the stack for the root's whole life, such as the bottom bar. It
  has a store and a controller but no screen component.
- The root host reads its stores from the root graph once and builds its controller once per root,
  over the root's lifecycle. It then works with no composition shown. The root host disposes its
  stores in `doOnDestroy`.
- Its route lives in the entry module, beside the root host that owns its controller. Example:
  [BottomNavigationBarRoute](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/BottomNavigationBarRoute.kt).
- Root content takes the root component and these controllers as one `@Immutable` bundle. Example:
  [RootDependencies](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/RootDependencies.kt).
- A root-level element's labels reach the root host through its controller's `onLabel`. The root
  host maps each label to a config and navigates, skipping the screen already shown.
- Where the element mirrors the stack, a root host subscription to the child stack sends the
  active root child's section to its store as an intent. It fires at once, before the first
  composition, and is cancelled in `doOnDestroy`. Example:
  [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt).
- An overlay over the root, such as a dialog, is a state holder the root host builds and root
  content draws. Example:
  [NotificationsRationaleState](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/NotificationsRationaleState.kt).

## Deep links

- A notification names its screen as the encoded root config. The navigation core module encodes
  and decodes it. Decoding never throws: a missing, malformed or unknown payload gives `null`.
  Example:
  [NavRootDeepLink](../../core-kmp/navigation/src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootDeepLink.kt).
- The payload key and the screen each notification opens live in the entry module, shared by both
  platforms. Example:
  [NotificationTapTarget](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/notification/NotificationTapTarget.kt).
- The notification module never sees the root configs. It asks external contracts for the payload
  or the intent, and the entry module implements them.
- Every payload is untrusted input; see [security-and-privacy.md](security-and-privacy.md).
- A tap navigates the live root through the root host's `openFromNotification`. It never rebuilds
  the root, and a screen already showing the target stays.
- Android: the activity reads the launching intent on a fresh start only, and the root opens on
  its target. A tap while the activity lives arrives through an `onNewIntent` listener. Example:
  [MainActivity](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt).
- The Android tap intent matches the launcher's intent and uses `NEW_TASK`, `CLEAR_TOP` and
  `SINGLE_TOP`, so a running activity gets it in `onNewIntent`. Example:
  [AnimeNotificationIntentProviderImpl](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/provider/AnimeNotificationIntentProviderImpl.kt).
- iOS: the notification delegate opens a screen only for the default tap action, on the main
  thread. A tap before the root exists is kept and applied when the root is built. Example:
  [NotificationTapDelegate](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/notification/NotificationTapDelegate.kt).
- What a tap does to saved state is in [state-restoration.md](state-restoration.md) "Scenarios".

## Adding a destination

1. Add a `data object` with a new `@SerialName` to the root configs.
2. In the feature module, add the screen component and its route in `impl/presentation/navigation`,
   as in the skeleton above and the route skeleton in
   [ui-compose.md "Screens and routes"](ui-compose.md#screens-and-routes).
3. Make the root graph implement the feature's dependencies contract and add a function that
   creates its feature graph; see [dependency-injection.md](dependency-injection.md)
   "Component hierarchy".
4. Add a root child that wraps the screen component and names its section.
5. Add the config's branch to the root host's child factory.
6. Add the root child's branch to root content, calling the route.
7. If the bar reaches it: add the section, the bar's label for it, and the label's branch in the
   root host's bar mapping.
8. If the screen keeps state, follow [state-restoration.md](state-restoration.md) "Adding saved
   state".
9. Cover it: the root host's navigation and bar mapping, and the screen component. Example:
   [RootHostTest](../../main/src/commonTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHostTest.kt).
   How tests are written is in [testing.md](testing.md).

The full sequence for a new screen feature is in [recipes.md](recipes.md) "New screen feature".

## Nested stacks

- As found, the app has one stack and no nested navigation.
- A screen that needs its own stack would own one in its screen component: Decompose's
  `childStack` over the component's context, with its own serializable config type. The parent
  root stack stays as it is.
