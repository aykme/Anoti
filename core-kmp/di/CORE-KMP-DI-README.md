Shared DI foundations. Its `androidMain` hosts the pre-existing cross-module Dagger component
contracts (`AppComponent`, `MainComponent`, and their `*External` accessors) that let feature
modules reach the app's and the main screen's Dagger graphs — unchanged until a later migration
phase retires them. The kotlin-inject-anvil scope markers, qualifier annotations, and
`PlatformContext` used to live here too; they moved to
[`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md), a genuinely zero-dependency leaf
module, to avoid a circular Gradle dependency (`core-kmp:di` depends on several leaf modules for
the legacy Dagger contracts above, so a leaf module depending back on `core-kmp:di` for scope
markers would cycle). `core-kmp:di` depends on `core-kmp:di-scope` in turn.

Its `commonMain` now hosts `TransitionalAppGraph`, the kotlin-inject-anvil bridge graph that
`app`'s still-Dagger component reads from while the App-scope migration is in progress (see
`TransitionalAppGraph`'s own KDoc). It grows by one accessor per migration phase (each documented
inline on the accessor itself, pointing back at the module/component it comes from) and is
deleted once `app` itself becomes a real `@MergeComponent`.

`TransitionalAppGraphKspAnchor` sits next to it as a required workaround, not a feature: on
Kotlin/Native targets only, kotlin-inject-anvil fails to find `@ContributesTo` contributions
declared in *other* modules unless the module holding the `@MergeComponent` also contributes
something of its own (upstream bug, project in maintenance mode — see the anchor's own KDoc for
the tracking issue). Keep it around for as long as `TransitionalAppGraph` exists and this module
contributes nothing else to `AppScope` on its own.

## How to include it

- Gradle: `implementation(project(":core-kmp:di"))`
- For the scope markers, qualifier annotations, and `PlatformContext`, depend on
  [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md) directly instead.
