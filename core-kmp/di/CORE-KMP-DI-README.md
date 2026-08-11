Shared DI foundations. Its `commonMain` hosts the kotlin-inject-anvil scope markers, qualifier
annotations, and the cross-platform `PlatformContext` handle that every later migration phase's
DI wiring builds on. Its `androidMain` also still hosts the pre-existing cross-module Dagger
component contracts (`AppComponent`, `MainComponent`, and their `*External` accessors) that let
feature modules reach the app's and the main screen's Dagger graphs — unchanged until a later
migration phase retires them. Neither is listed below since this README indexes `commonMain`
only.

## Entities

- [AppScope, ActivityScope, FeatureScope](src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/scope/Scope.kt) —
  kotlin-inject-anvil scope markers for `@ContributesTo`/`@ContributesBinding`/`@SingleIn`/
  `@MergeComponent`.
- [AppContext, ActivityContext, AnimeBackgroundUpdate](src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/qualifier/Qualifier.kt) —
  kotlin-inject qualifier annotations disambiguating same-typed dependencies.
- [PlatformContext](src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/PlatformContext.kt) —
  cross-platform application-context handle.

## How to include it

- Gradle: `implementation(project(":core-kmp:di"))`
- These are foundational types, not injected values: annotate your own `@Inject`/`@Provides`
  declarations with the scope/qualifier annotations, and use `PlatformContext` as the
  commonMain-safe parameter/return type wherever `android.content.Context` would otherwise leak
  into a `commonMain` signature. None of them has DI wiring of its own, and no phase yet
  assembles a real `@MergeComponent` graph that provides a `PlatformContext` instance.
