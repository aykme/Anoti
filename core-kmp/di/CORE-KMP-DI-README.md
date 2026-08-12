The iOS app-scope graph. Hosts `IosAppGraph`, the `@MergeComponent(AppScope::class)` an iOS host
app would create once at startup — the mirror of `:app`'s Android `AppGraph`. Until that host app
exists, it also serves as the compile-time proof that every iOS binding in the repo actually
wires together. The scope markers, qualifier annotations, and `PlatformContext` live
in [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md), a zero-dependency leaf module,
so leaf modules can depend on them without cycling back through this one; `core-kmp:di` depends on
`core-kmp:di-scope` in turn.

`IosAppGraphKspAnchor` sits next to it as a required workaround, not a feature: on Kotlin/Native
targets only, kotlin-inject-anvil fails to find `@ContributesTo` contributions declared in *other*
modules unless the module holding the `@MergeComponent` also contributes something of its own
(upstream bug, project in maintenance mode — see the anchor's own KDoc for the tracking issue).
Keep it around for as long as this module contributes nothing else to `AppScope` on its own.

## How to include it

- Gradle: `implementation(project(":core-kmp:di"))` — only an iOS host app needs this; nothing on
  Android depends on it.
- `IosAppGraph::class.create()` builds the graph; read its accessors instead of constructing the
  values yourself.
- For the scope markers, qualifier annotations, and `PlatformContext`, depend on
  [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md) directly instead.
