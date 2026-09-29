Root navigation for the app: the screens reachable from the root stack, the Decompose component
that owns navigation between them, and the text form a notification carries to open one.

## Entities

- [NavRootConfig](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootConfig.kt) —
  a screen reachable from the root navigation stack.
- [NavRootComponent](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootComponent.kt) —
  owns the root navigation stack and drives navigation between `NavRootConfig` screens.
- [NavRootDeepLink](src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootDeepLink.kt) —
  writes and reads a `NavRootConfig` as a notification's payload.

## How to include it

- Gradle: `implementation(project(":core-kmp:navigation"))`
- `NavRootComponent` has no DI wiring; a consumer constructs it directly, passing its own
  `ComponentContext` and a `childFactory` that turns each `NavRootConfig` into that app's
  screen type. `NavRootDeepLink` is an object, called directly.

## How to use it

```kotlin
// From the root host in `main`: the platform supplies componentContext, and childFactory maps
// each NavRootConfig to the app's screen type.
val root = NavRootComponent(
    componentContext = componentContext,
    initialConfiguration = openingTarget ?: NavRootConfig.AnimeList,
    childFactory = ::createRootChild
)
```
