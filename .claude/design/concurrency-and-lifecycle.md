# Concurrency and lifecycle

Which coroutine context code runs in, who owns each scope and cancels it, what runs on the main
thread, and how Essenty lifecycles drive binders, one-time hooks and disposal. Exception handlers
and what a failure shows the user are owned by [error-handling.md](error-handling.md).

Read when: launching a coroutine or creating a scope; picking a context or dispatcher; binding
stores to a lifecycle; adding a start or destroy hook; deciding who disposes a store.

## Coroutine contexts

- Every context and dispatcher comes from the injected `CoroutineContextProvider` of the UI kit
  module. Example:
  [CoroutineContextProvider](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/coroutinecontext/CoroutineContextProvider.kt).
- Its contexts differ in the job they carry, and so in who owns the work:

| Member | Carries | Use it for |
|---|---|---|
| `newMainCoroutineContext()` | Main, the app's handler, a fresh `SupervisorJob` per call | A scope that owns and cancels its own work, such as an executor's |
| `appMainCoroutineContext` | Main, the app's handler, one `SupervisorJob` shared by the app | Work that must finish after whatever started it is gone |
| `mainCoroutineContext` | Main and the app's handler, no job | The base the two above build on |
| `workManagerCoroutineContext` | IO and an empty handler, no job | Background work, so the worker's own cancellation reaches it |
| `ioDispatcher`, `defaultDispatcher`, `mainDispatcher`, `unconfinedDispatcher` | A dispatcher only | `withContext` around blocking or CPU-bound calls |

- The provider's base class builds these; its production implementations differ only in the
  handler, and the fake also takes the IO, default and background contexts. Example:
  [CoroutineContextProviderBase](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/domain/coroutinecontext/CoroutineContextProviderBase.kt).
- Tests hand test dispatchers to the provider's fake; see [testing.md](testing.md).
- Named exceptions build their own context:
  - Room's query context, set in the database builder; see [data-layer.md](data-layer.md)
    "Persistence".
  - As found: the iOS update-pass scope builds its own `SupervisorJob` and a handler that logs.
    An unhandled throw would otherwise end the process on that platform. Example:
    [DiAnimeBackgroundUpdatePlatformComponent](../../feature-kmp/anime-background-update/src/iosMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/ios/impl/di/DiAnimeBackgroundUpdatePlatformComponent.kt).
  - As found: the iOS root holder's permission check builds a main scope cancelled with the root.
    Example:
    [IosRootHolder](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/IosRootHolder.kt).

## Scope owners

- An executor passes `newMainCoroutineContext()` to `CoroutineExecutor` as its `mainContext`.
  Its `scope` is cancelled when the store is disposed. Executors are owned by [mvi.md](mvi.md)
  "Executors and state".
- A write that must outlive the screen runs on a scope over `appMainCoroutineContext`. Reads stay
  on the executor's `scope`, since nothing is left to render them. Example:
  [AnimeDatabaseExecutorImpl](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/domain/store/AnimeDatabaseExecutorImpl.kt).
- The Android `Application` launches its start-up on `appMainCoroutineContext` and moves the
  blocking part to `ioDispatcher`. Example:
  [AnotiApp](../../androidApp/src/main/kotlin/com/alekseivinogradov/anoti/impl/presentation/AnotiApp.kt).
- Background work wraps its body in `withContext(workManagerCoroutineContext)`. Example:
  [AnimeUpdateManagerImpl](../../feature-kmp/anime-background-update/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/domain/manager/AnimeUpdateManagerImpl.kt).
- UI code launches from a scope Compose owns, such as a `Modifier.Node`'s `coroutineScope`, which
  ends when the node detaches. Example:
  [RepeatingClickable](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/presentation/compose/RepeatingClickable.kt).

## Main thread

- Stores, controllers, the root host and the iOS root holder run on the main thread. The entry
  module's platform-facing classes say "Main thread only" in their KDoc, or on the methods a
  platform calls. Example:
  [IosRootHolder](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/IosRootHolder.kt),
  [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt).
- Executors call suspending usecases from the main context and switch no dispatcher themselves.
- Platform code that blocks, such as posting a notification, switches to `ioDispatcher` inside its
  own suspending function. Example:
  [AnimeNotificationManagerImpl](../../feature-kmp/anime-notification/src/androidMain/kotlin/com/alekseivinogradov/anoti/animenotification/android/impl/presentation/manager/AnimeNotificationManagerImpl.kt).
- A platform callback with no thread guarantee hops to the main thread before it touches shared
  state. Example:
  [NotificationTapDelegate](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/notification/NotificationTapDelegate.kt).

## Flows from stores

- A controller exposes state and binds stores over its owner's lifecycle, as
  [mvi.md "Controllers"](mvi.md#controllers) says.
- The binder's collection starts on a later main-thread turn. Work that must reach a store before
  it, such as a restore replay, sends intents to the store directly.

## Cancellation

- A catch-all around suspending work rethrows `CancellationException` first; see
  [error-handling.md "Catching"](error-handling.md#catching).
- Cleanup that must run even when the scope is already gone hangs off the job with
  `invokeOnCompletion`, not a `finally` inside it. A coroutine whose scope is cancelled before it
  starts never runs its body. Example:
  [AnimeDatabaseExecutorImpl](../../core-kmp/anime-database/src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/impl/domain/store/AnimeDatabaseExecutorImpl.kt).
- A `Job` handle on an executor guards against starting the same work twice; see [mvi.md](mvi.md)
  "Executors and state".

## Lifecycles

- Lifecycles are Essenty's. Each owner gets one from its platform:
  - Android: the root follows the activity, through `defaultComponentContext()`.
  - iOS: the root follows the app's lifecycle through a child lifecycle that can also be destroyed
    on its own. Example:
    [ChildLifecycle](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/lifecycle/ChildLifecycle.kt).
  - A screen component gets its lifecycle from the root stack: created when its config becomes
    active, destroyed when the stack replaces it.
- Hooks used:
  - `doOnDestroy` disposes stores and cancels subscriptions, such as the root host's child-stack
    subscription.
  - `doOnStart(isOneTime = true)` runs a screen's opening or restore replay once, on its first
    start. Its order against the controller is in [state-restoration.md](state-restoration.md)
    "Screen restore protocol".
  - `doOnResume(isOneTime = true)` waits for the app to be in front, as the iOS permission check
    does.
- Binders follow the owner's lifecycle with `START_STOP`, as above.

## Disposal

- The creator disposes. Store bindings are unscoped, so each read of one builds a new instance,
  and whoever reads it owns it. Scopes and bindings are owned by
  [dependency-injection.md](dependency-injection.md) "Scopes".
- An owner reads each store once and disposes it in `doOnDestroy`. The hook is registered before
  anything that can throw, so a failed build still closes its stores. Examples:
  [RootHost](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/RootHost.kt),
  [NavAnimeFavoritesScreenComponent](../../feature-kmp/anime-favorites/src/commonMain/kotlin/com/alekseivinogradov/anoti/animefavorites/kmp/impl/presentation/navigation/NavAnimeFavoritesScreenComponent.kt).
- Nothing else disposes a store it did not create.

## Tests

- Virtual time, test dispatchers and the provider's fake are covered in [testing.md](testing.md)
  "Virtual time".
