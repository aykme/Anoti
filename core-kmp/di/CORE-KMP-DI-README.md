Shared DI foundations. Its `androidMain` hosts the pre-existing cross-module Dagger component
contracts (`AppComponent`, `MainComponent`, and their `*External` accessors) that let feature
modules reach the app's and the main screen's Dagger graphs — unchanged until a later migration
phase retires them. Its `commonMain` currently has no entities of its own: the
kotlin-inject-anvil scope markers, qualifier annotations, and `PlatformContext` that used to
live here moved to [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md), a genuinely
zero-dependency leaf module, to avoid a circular Gradle dependency (`core-kmp:di` depends on
several leaf modules for the legacy Dagger contracts above, so a leaf module depending back on
`core-kmp:di` for scope markers would cycle). `core-kmp:di` depends on `core-kmp:di-scope` in
turn, and hosts the kotlin-inject-anvil `TransitionalAppGraph`/`IosAppGraph` component(s) once a
later phase introduces them.

## How to include it

- Gradle: `implementation(project(":core-kmp:di"))`
- For the scope markers, qualifier annotations, and `PlatformContext`, depend on
  [`core-kmp:di-scope`](../di-scope/CORE-KMP-DI-SCOPE-README.md) directly instead.
