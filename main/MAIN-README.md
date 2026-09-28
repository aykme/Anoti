The app's entry module: the root UI host's dependency graph, the root content with its
navigation, and the Android entry point.

## Entities

- [DiRootDependencies](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/api/di/DiRootDependencies.kt) —
  what the root UI graph takes from the app-wide component.
- [DiRootComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/di/DiRootComponent.kt) —
  the root UI host's dependency graph, one per host.
- [DiRootComponentHolder](src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/di/DiRootComponentHolder.kt) —
  lets a host create its `DiRootComponent`.
- [MainActivity (Android)](src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/MainActivity.kt) —
  the Android entry point.

## How to include it

- Gradle: `implementation(project(":main"))`
- `DiRootDependencies` is implemented by `DiAppComponent` in
  [`core-kmp:di-app`](../core-kmp/di-app/CORE-KMP-DI-APP-README.md).
- `DiRootComponent::class.create(appComponent)` builds the graph from that component.
- `MainActivity` is declared in this module's manifest and started by the system.

## How to use it

The app's `Application` implements `DiRootComponentHolder` and returns a new `DiRootComponent`
from every call. `MainActivity` asks it for one each time it is created, takes the stores from
it, and builds the root navigation.
