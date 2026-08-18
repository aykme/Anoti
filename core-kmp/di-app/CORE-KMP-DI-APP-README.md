Hosts both platforms' app-wide composition roots: `DiAppComponent`, the root of the
`AppScope` → `RootScope` → `FeatureScope` hierarchy, one per platform. The scope annotations,
qualifier annotations, and `PlatformContext` live in
[`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md), a zero-dependency leaf module, so
leaf modules can depend on them without cycling back through this one; `core-kmp:di` depends on
`core-kmp:di-scope` in turn. It also depends on `:main`, whose root UI host graph it builds.

- [`DiAppComponent` (Android)](src/androidMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt)
  — `:app`'s root, created once in `AnotiApp.onCreate`.
- [`DiAppComponent` (iOS)](src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt)
  — the iOS mirror, an iOS host app would create it the same way. Until that host app exists, it
  also serves as the compile-time proof that every iOS binding in the repo actually wires
  together.

## How to include it

- Gradle: `implementation(project(":core-kmp:di-app"))`.
- `DiAppComponent::class.create(appContext)` builds the graph on either platform; read its
  accessors instead of constructing the values yourself.
- `DiRootComponent::class.create(appComponent)` builds `:main`'s `DiRootComponent`, the root UI
  host's graph, from the component above.
- For the scope annotations, qualifier annotations, and `PlatformContext`, depend on
  [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md) directly instead.
