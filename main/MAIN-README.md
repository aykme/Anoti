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
from every call. `MainActivity` asks it for one each time it is created and hands it to a new
`RootHost`. With it go the screen a tapped notification names, a way to build its component
context, and its notification-permission requests. The host builds the root navigation, and the
activity shows the host's content.

On iOS, `IosScreenHost` lives for the whole process, and each composition of its screen builds a
new `RootHost`. A tapped notification replaces that `RootHost` with one opening on the screen it
names. The root's state goes to a file when the app goes to the background, and the next
process restores it from there.
