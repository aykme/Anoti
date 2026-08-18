Root navigation for the app: the screens reachable from the root stack and the Decompose
component that owns navigation between them.

## Entities

- [NavRootConfig](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootConfig.kt) —
  a screen reachable from the root navigation stack.
- [NavRootComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootComponent.kt) —
  owns the root navigation stack and drives navigation between `NavRootConfig` screens.

## How to include it

- Gradle: `implementation(project(":core-kmp:navigation"))`
- `NavRootComponent` has no DI wiring; a consumer constructs it directly, passing its own
  `ComponentContext` and a `childFactory` that turns each `NavRootConfig` into that platform's
  screen type.

## How to use it

```kotlin
// Android example, from MainActivity — componentContext comes from the Activity's own
// defaultComponentContext(); childFactory maps each NavRootConfig to that platform's screen type.
val root = NavRootComponent(
    componentContext = defaultComponentContext(),
    childFactory = ::createRootChild
)
```
