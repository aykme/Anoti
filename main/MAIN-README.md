The app's entry module: the root UI host's dependency graph, the root content with its
navigation, and the screen hosts of both platforms.

## Entities

- [DiRootDependencies](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/api/di/DiRootDependencies.kt) —
  what the root UI graph takes from the app-wide component.
- [DiRootComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/di/DiRootComponent.kt) —
  the root UI host's dependency graph, one per host.
- [DiRootComponentHolder](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/di/DiRootComponentHolder.kt) —
  lets a host create its `DiRootComponent`.
- [RootHost](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt) —
  a screen host's shared work around the root UI.
- [MainActivity (Android)](src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt) —
  the Android entry point.

## How to include it

- Gradle: `implementation(project(":main"))`
- `DiRootDependencies` is implemented by `DiAppComponent` in
  [`core-kmp:di-app`](../core-kmp/di-app/CORE-KMP-DI-APP-README.md).
- `createDiRootComponent(appComponent)` builds the graph from that component on every platform.
  Android code can also call `DiRootComponent::class.create(appComponent)`.
- `MainActivity` is declared in this module's manifest and started by the system.
- On iOS, `IosScreenHost` is the counterpart of `MainActivity`. `IosApp` in `core-kmp:di-app`
  builds one over its graph and hands out its screen.

## How to use it

The app's `Application` implements `DiRootComponentHolder` and returns a new `DiRootComponent`
from every call. It also holds the process's one `NotificationPermissionSession`. `MainActivity`
asks it for both each time it is created and hands them to a new `RootHost`. With them go the
screen a tapped notification names, a way to build its component context, and its
notification-permission requests. The host builds the root navigation, and the
activity shows the host's content. A notification tapped while the activity runs opens its screen
in the same root.

On iOS, `IosScreenHost` lives for the whole process and holds the app's one `RootHost`, built the
first time the scene asks for its screen. The scene keeps the root's state as one string in its
`@SceneStorage`, which the host writes and reads back. A tapped notification opens its screen in
that root, or in the one still to be built.

A screen whose smaller side is under 600 keeps the app upright on both platforms. iOS measures
the window in points: `iosSupportedInterfaceOrientations(window)` answers from its size, and
`IosApp` hands that answer to the app delegate. Android measures the whole display in dp at the
density the device ships with, so the display size setting never makes a tablet narrow.
