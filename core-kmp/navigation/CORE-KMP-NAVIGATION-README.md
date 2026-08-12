Root navigation for the app: the screens reachable from the root stack and the Decompose
component that owns navigation between them.

## Entities

- [RootConfig](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/RootConfig.kt) —
  a screen reachable from the root navigation stack.
- [RootComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/RootComponent.kt) —
  owns the root navigation stack and drives navigation between `RootConfig` screens.

## How to include it

- Gradle: `implementation(project(":core-kmp:navigation"))`
- `RootComponent` has no DI wiring; a consumer constructs it directly, passing its own
  `ComponentContext` and a `childFactory` that turns each `RootConfig` into that platform's
  screen type.

## How to use it

```kotlin
// iOS example (no Android example yet):
val lifecycle = LifecycleRegistry()
val root = RootComponent(
    componentContext = DefaultComponentContext(lifecycle = lifecycle),
    childFactory = { config, _ -> config }
)
```
