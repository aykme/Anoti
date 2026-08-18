The iOS app-wide component. Hosts `DiAppComponent`, the root of the `AppScope` → `RootScope` →
`FeatureScope` hierarchy an iOS host app would create once at startup via
`DiAppComponent::class.create(appContext)` — the mirror of `:app`'s Android `DiAppComponent`.
Until that host app exists, it also serves as the compile-time proof that every iOS binding in
the repo actually wires together. The scope annotations, qualifier annotations, and
`PlatformContext` live in [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md), a
zero-dependency leaf module, so leaf modules can depend on them without cycling back through this
one; `core-kmp:di` depends on `core-kmp:di-scope` in turn. It also depends on `:main`, whose root
UI host graph it builds.

## How to include it

- Gradle: `implementation(project(":core-kmp:di"))` — only an iOS host app needs this; nothing on
  Android depends on it.
- `DiAppComponent::class.create(appContext)` builds the graph; read its accessors instead of
  constructing the values yourself.
- `DiRootComponent::class.create(appComponent)` builds `:main`'s `DiRootComponent`, the root UI
  host's graph, from the component above.
- For the scope annotations, qualifier annotations, and `PlatformContext`, depend on
  [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md) directly instead.
