Hosts both platforms' app-wide composition roots: `DiAppComponent`, the root of the
`AppScope` → `RootScope` → `FeatureScope` hierarchy, one per platform. It also holds `IosApp`,
the iOS app's entry point. The scope annotations, qualifier annotations, and `PlatformContext`
live in [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md), a zero-dependency leaf
module, so leaf modules can depend on them without cycling back through this one.
`core-kmp:di-app` depends on `core-kmp:di-scope` in turn. It also depends on `:main` for
`DiRootDependencies`, which `DiAppComponent` implements.

- [`DiAppComponent` (Android)](src/androidMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt)
  — `:androidApp`'s root, created once in `AnotiApp.onCreate`.
- [`IosApp` (iOS)](src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/IosApp.kt)
  — the iOS app's entry point, the only thing its Swift code calls.

## How to include it

- Gradle: `implementation(project(":core-kmp:di-app"))`.
- On iOS the module links `Shared`, the project's one framework. No other module declares one.
- On Android, `DiAppComponent::class.create(appContext)` builds the graph; read its accessors
  instead of constructing the values yourself.
- On iOS, `IosApp.start()` builds the graph, an internal `DiAppComponent` the framework does not
  show. It also builds `:main`'s screen host over it. `IosApp.viewController()` then hands out
  the app's screen.
- `createDiRootComponent(appComponent)` builds `:main`'s `DiRootComponent`, the root UI host's
  graph, from the component above on either platform.
- For the scope annotations, qualifier annotations, and `PlatformContext`, depend on
  [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md) directly instead.
