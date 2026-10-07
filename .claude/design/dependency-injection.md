# Dependency injection

How objects are wired: kotlin-inject components, their scopes and lifetimes, the hierarchy from the
app graph down to a feature graph, where module and platform components plug in, how components are
created, and who builds each graph. Store and executor bindings are in [mvi.md](mvi.md); disposal
of what a graph hands out is in [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md).

Read when: adding or changing a DI binding, a `Di*Component`, a `Di*Dependencies` contract, a scope
or a qualifier; adding a module that contributes bindings; building a graph in a host or a test.

## Library

- The graphs are kotlin-inject components, generated at build time by its KSP processor. Why it
  was chosen is in [decisions.md](decisions.md).
- kotlin-inject's KSP processor runs only in modules that declare a `@Component`, once per target;
  the Gradle lines are in [new-module.md](new-module.md).
  Example: [anime-list build file](../../feature-kmp/anime-list/build.gradle.kts).
- Scope and qualifier annotations and `PlatformContext` live in the DI-scope leaf module, so any
  module uses them without depending on the composition root.
  Example: [Scope.kt](../../core-kmp/di-scope/src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/scope/Scope.kt).

## Scopes

| Scope | Marks | One instance per |
|---|---|---|
| `@AppScope` | the app graph and its shared bindings | process |
| `@RootScope` | the root graph, the parent of every feature graph | root host |
| `@FeatureScope` | a feature graph | screen component |

- A binding is `@AppScope` when every reader must share its instance: the HTTP client, `SafeApi`,
  the database, `StoreFactory`, `CoroutineContextProvider`, the system message controller. Where
  the shared instance is the whole point, the binding's KDoc says so.
  Example: [DiCelebrityComponent](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/di/DiCelebrityComponent.kt).
- As found: some stateless bindings of app-wide modules are `@AppScope` too: the service, the
  repository, a background source and usecase, and the entry module's tap and intent providers.
  Example: [DiAnimeBaseComponent](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/di/DiAnimeBaseComponent.kt),
  [DiAnimeBackgroundUpdateComponent](../../feature-kmp/anime-background-update/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/di/DiAnimeBackgroundUpdateComponent.kt).
- Every other binding is unscoped: each read builds a new instance. That covers every store, most
  usecases and stateless helpers. A store has one instance per reader; see
  [decisions.md](decisions.md) and [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md).
- `@RootScope` and `@FeatureScope` mark components only; no binding carries them today.

## Component hierarchy

- The app graph lives in the composition root, one twin per platform source set, with the same
  name and package. It mixes in every app-wide module and platform component and implements
  `DiRootDependencies`.
  Example: [Android DiAppComponent](../../core-kmp/di-app/src/androidMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt).
- `DiRootDependencies` is the root graph's dependencies contract. It lives in the entry module's
  `api/di`, so the root graph never names the app graph's concrete type.
  Example: [DiRootDependencies](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/api/di/DiRootDependencies.kt).
- The root graph lives in the entry module's `commonMain`, with `@Component val parent:
  DiRootDependencies`. It implements every feature's dependencies contract, so it is the parent of
  each feature graph. It has one `createDi<Feature>Component()` per screen.
  Example: [DiRootComponent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/di/DiRootComponent.kt).
- A feature's dependencies contract, `Di<Feature>Dependencies`, lists as `val`s only what its
  feature graph reads from the parent. It lives in the feature's `api/di`. The feature graph,
  `Di<Feature>Component`, lives in `impl/di` and exposes as abstract `val`s the screen's stores
  and any value its screen needs, such as a date formatter.
  The root host's child factory builds one per screen component; see
  [navigation.md](navigation.md), section "Root stack".
  Example: [DiAnimeFavoritesDependencies](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/api/di/DiAnimeFavoritesDependencies.kt).

## Module and platform components

- A module component is an interface `Di<Module>Component` in the module's `kmp/impl/di` package.
  It holds only `@Provides` functions with bodies. It is mixed into the app graph, or into the root
  graph for a root-level element.
  Example: [DiBottomNavigationBarComponent](../../feature-kmp/bottom-navigation-bar/src/commonMain/kotlin/com/alekseivinogradov/anoti/bottomnavigationbar/kmp/impl/di/DiBottomNavigationBarComponent.kt).
- A platform component is an interface `Di<Module>PlatformComponent`, one in `androidMain` and one
  in `iosMain`, with the same simple name. Each app graph twin mixes in its own platform's. The
  naming of platform counterparts is in [module-anatomy.md](module-anatomy.md), section "Naming".
- A feature that reads a contract of an external module consumes the binding and never provides
  it. The entry module implements the contract and contributes the binding from its own component,
  mixed into the app graph. Android and iOS use different components here; see
  [platform-mirroring.md](platform-mirroring.md), section "Accepted asymmetries".
  Example: [DiRootPlatformComponent](../../main/src/androidMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/di/DiRootPlatformComponent.kt).
- The packages of the Android platform components are not uniform; see
  [module-anatomy.md](module-anatomy.md), section "Packages".

## Bindings

- Most bindings are `@Provides` functions that build the implementation, naming the arguments when
  there are several ([code-style.md "Calls and lambdas"](code-style.md#calls-and-lambdas)).
  Example: [DiNetworkComponent](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/di/DiNetworkComponent.kt).
- As found: the screen store factories take `(storeFactory, executorFactory)` positionally.
  Example: [DiAnimeListComponent](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/di/DiAnimeListComponent.kt).
- Four classes carry `@Inject` instead. Each is read from the graph directly, or bound to its
  contract by a `@Provides` function that takes the implementation as its parameter.
  Example: [AnimeNotificationChannelFactory](../../feature-kmp/anime-notification/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/factory/AnimeNotificationChannelFactory.kt).
- A qualifier tells same-typed bindings apart: `@AppContext` for the app's `PlatformContext`, a
  feature qualifier for a feature's own platform objects. Both live in the DI-scope leaf module.
  Example: [Qualifier.kt](../../core-kmp/di-scope/src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/qualifier/Qualifier.kt).
- Both app graph twins take the platform context
  ([kmp.md "Expect and actual"](kmp.md#expect-and-actual)) as
  `@get:Provides @AppContext val appContext`, so both are built alike.
  Example: [PlatformContext.kt](../../core-kmp/di-scope/src/commonMain/kotlin/com/alekseivinogradov/anoti/di/kmp/PlatformContext.kt).
- A function type `() -> T` as a `@Provides` parameter defers building `T` until it is called, so
  an object built at launch does not build its dependencies then.
  Example: [iOS DiAnimeBackgroundUpdatePlatformComponent](../../feature-kmp/anime-background-update/src/iosMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/ios/impl/di/DiAnimeBackgroundUpdatePlatformComponent.kt).
- A platform singleton that asks the graph for its own configuration is not bound. The binding
  passes a lambda that fetches it, so it is never reached while the graph is locked.
  Example: [Android DiAnimeBackgroundUpdatePlatformComponent](../../feature-kmp/anime-background-update/src/androidMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/android/impl/presentation/di/DiAnimeBackgroundUpdatePlatformComponent.kt).

## Creating graphs

- A component built from common code has a creator next to it,
  `@KmpComponentCreate expect fun createDi<Name>Component(...)`, whose body KSP writes per target.
  The root graph and every feature graph have one.
- Android code may also call the generated `Di<Name>Component::class.create(...)`. The Android app
  graph is built only that way, the iOS app graph only through its `internal` creator.
- The Android host's `Application` builds the app graph in `onCreate`. It implements
  `DiRootComponentHolder`, so the screen host in the entry module gets a new root graph without
  seeing the app graph's type.
  Example: [AnotiApp](../../androidApp/src/main/kotlin/com/alekseivinogradov/anoti/impl/presentation/AnotiApp.kt).
- On iOS the entry object builds the app graph and hands it to the screen host, which builds the
  root graph with the root; see [platform-mirroring.md](platform-mirroring.md), section "Startup".
- The iOS app graph stays `internal`; [ios-host.md](ios-host.md) says why. Its compiling proves
  that every iOS binding wires together; see [build-check.md](../rules/ios/build-check.md).

## Testing a component

- How a test builds a feature or root graph is in
  [testing.md "DI components in tests"](testing.md#di-components-in-tests).
  Example: [HostApplicationFake](../../main/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/HostApplicationFake.kt).
- A test implements a module or platform component with an empty `object` and calls its
  `@Provides` functions directly. Those bodies count toward
  [coverage](../rules/test-coverage.md).
  Example: [DiAnimeBackgroundUpdatePlatformComponentTest](../../feature-kmp/anime-background-update/src/androidHostTest/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/android/impl/presentation/di/DiAnimeBackgroundUpdatePlatformComponentTest.kt).

## Skeletons

```kotlin
// A module component, mixed into the app graph
package <root-package>.<module-id>.kmp.impl.di

interface Di<Module>Component {
    @Provides
    @AppScope // only when every reader must share the instance
    fun provide<Name>(dependency: <Dependency>): <Name> = <Name>Impl(dependency)
}
```

Mirrors: [DiAnimeBaseComponent](../../feature-kmp/anime-base/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebase/kmp/impl/di/DiAnimeBaseComponent.kt)

```kotlin
// A platform component pair. androidMain, package <root-package>.<module-id>.android.impl.di
interface Di<Module>PlatformComponent {
    @Provides
    @AppScope
    fun provide<Name>(@AppContext appContext: PlatformContext): <Name> = build<Name>(appContext)
}

// iosMain, package <root-package>.<module-id>.ios.impl.di
interface Di<Module>PlatformComponent {
    @Provides
    @AppScope
    fun provide<Name>(): <Name> = build<Name>()
}
```

Mirrors: [Android DiAnimeDatabasePlatformComponent](../../core-kmp/anime-database/src/androidMain/kotlin/com/alekseivinogradov/anoti/animedatabase/android/impl/di/DiAnimeDatabasePlatformComponent.kt), [iOS DiAnimeDatabasePlatformComponent](../../core-kmp/anime-database/src/iosMain/kotlin/com/alekseivinogradov/anoti/animedatabase/ios/impl/di/DiAnimeDatabasePlatformComponent.kt)

```kotlin
// A dependencies contract, package <root-package>.<module-id>.kmp.api.di
interface Di<Feature>Dependencies {
    val storeFactory: StoreFactory
    val coroutineContextProvider: CoroutineContextProvider
}

// Its feature graph, package <root-package>.<module-id>.kmp.impl.di
@Component
@FeatureScope
abstract class Di<Feature>Component(
    @Component val parent: Di<Feature>Dependencies
) {
    abstract val mainStore: <Feature>MainStore
    abstract val dateFormatter: DateFormatter
    @Provides
    fun provide<Feature>ExecutorFactory(
        coroutineContextProvider: CoroutineContextProvider
    ): <Feature>ExecutorFactory = {
        <Feature>ExecutorImpl(coroutineContextProvider = coroutineContextProvider)
    }
    @Provides
    fun provide<Feature>MainStore(
        storeFactory: StoreFactory,
        executorFactory: <Feature>ExecutorFactory
    ): <Feature>MainStore = <Feature>MainStoreFactory(storeFactory, executorFactory).create()
}

@KmpComponentCreate
expect fun createDi<Feature>Component(parent: Di<Feature>Dependencies): Di<Feature>Component
```

Mirrors: [DiAnimeFavoritesComponent](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/di/DiAnimeFavoritesComponent.kt)

```kotlin
// The root graph, in the entry module's commonMain
@Component
@RootScope
abstract class DiRootComponent(
    @Component val parent: DiRootDependencies
) : Di<Element>Component, Di<Feature>Dependencies {
    abstract val <element>Store: <Element>Store
    fun createDi<Feature>Component(): Di<Feature>Component =
        createDi<Feature>Component(parent = this)
}

@KmpComponentCreate
expect fun createDiRootComponent(parent: DiRootDependencies): DiRootComponent

// The app graph twins, in the composition root's androidMain and iosMain (internal there)
@Component
@AppScope
abstract class DiAppComponent(
    @get:Provides @AppContext val appContext: PlatformContext
) : Di<Module>Component, Di<Module>PlatformComponent, DiRootDependencies

// iosMain only
@KmpComponentCreate
internal expect fun createDiAppComponent(appContext: PlatformContext): DiAppComponent
```

Mirrors: [DiRootComponent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/di/DiRootComponent.kt), [Android DiAppComponent](../../core-kmp/di-app/src/androidMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt), [iOS DiAppComponent](../../core-kmp/di-app/src/iosMain/kotlin/com/alekseivinogradov/anoti/di/kmp/DiAppComponent.kt)
