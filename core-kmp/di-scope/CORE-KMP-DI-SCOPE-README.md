Zero-dependency leaf module for shared DI plumbing. Hosts the kotlin-inject scope markers,
qualifier annotations, and the cross-platform `PlatformContext` handle that all the app's DI
wiring builds on. Split out of `core-kmp:di` (see that module's README) specifically so leaf
modules can depend on these types without creating a circular Gradle dependency with
`core-kmp:di`, which depends on several leaf modules of its own.

## Entities

- [AppScope, RootScope, FeatureScope](src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/scope/Scope.kt) —
  kotlin-inject `@Scope` annotations marking bindings held by the app-wide, root-host, and
  screen-level components respectively.
- [AppContext, AnimeBackgroundUpdate](src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/qualifier/Qualifier.kt) —
  kotlin-inject qualifier annotations disambiguating same-typed dependencies.
- [PlatformContext](src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/PlatformContext.kt) —
  cross-platform application-context handle.

## How to include it

- Gradle: `implementation(project(":core-kmp:di-scope"))`
- These are foundational types, not injected values: annotate your own `@Inject`/`@Provides`
  declarations with the scope/qualifier annotations, and use `PlatformContext` as the
  commonMain-safe parameter/return type wherever `android.content.Context` would otherwise leak
  into a `commonMain` signature. None of them has DI wiring of its own; the app-scoped
  `PlatformContext` instance itself is supplied to `:app`'s `DiAppComponent` when it is created.
