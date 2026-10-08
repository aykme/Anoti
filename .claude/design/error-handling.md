# Error handling

How a failure becomes something the user sees, or a log line nobody sees: the mapping from a
call's failure to a system message, the path that message takes to the screen, and the coroutine
exception handlers behind every context the app launches work in.

Read when: handling a failed call or page load, showing an error to the user, adding a coroutine
scope or context, or catching an exception.

## Failure to message

- A failed call arrives as a `CallResult` failure; see [data-layer.md](data-layer.md), section
  "Network". The executor maps it with an exhaustive `when`:
  - `HttpError` and `NetworkError` show the connection error message;
  - `OtherError` shows the unknown error message.
- A paged load maps the same way: `PageLoadResult.Error` shows the connection error,
  `PageLoadResult.UnexpectedError` the unknown error.
- A failed first page also moves the section's content type to its error state. A failed next
  page shows the message only and keeps the items already shown.
  Example: [OngoingSectionExecutorImpl](../../feature-kmp/anime-list/src/commonMain/kotlin/com/alekseivinogradov/anoti/animelist/kmp/impl/domain/store/ongoingsection/OngoingSectionExecutorImpl.kt)
- An `OtherError` is not a connectivity problem, so it gets the unknown error message. It covers
  response bodies that fail to decode and unexpected throws.
- Background work shows no message. It turns a failure into its work result and logs it.
  Example: [AnimeUpdateManagerImpl](../../feature-kmp/anime-background-update/src/commonMain/kotlin/com/alekseivinogradov/anoti/animebackgroundupdate/kmp/impl/domain/manager/AnimeUpdateManagerImpl.kt)

## Message path

- The executor calls `SystemMessageProvider`, injected through its constructor. The provider holds
  two callbacks: `makeConnectionErrorSystemMessage` and `makeUnknownErrorSystemMessage`.
- The UI kit module's component binds both callbacks to `SystemMessageController.show` with the
  matching string resource. Both are `@AppScope`.
  Example: [DiCelebrityComponent](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/di/DiCelebrityComponent.kt)
- `SystemMessageController` is a stream of string resources, safe to call from any thread. A
  message sent while nothing collects is dropped. A busy collector gets only the latest one.
  Example: [SystemMessageController](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/api/domain/systemmessage/controller/SystemMessageController.kt)
- `SystemMessageHost` shows the messages as Material 3 snackbars; a newer one replaces the one on
  screen. The root content places it once, over every screen.
  Example: [RootContent](../../main/src/commonMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/compose/RootContent.kt)

## Exception handlers

- Contexts come from `CoroutineContextProvider`. `CoroutineContextProviderBase` builds them around
  one handler that passes every uncaught throwable to `exceptionHandlerCallback`.
  Example: [CoroutineContextProviderBase](../../core-kmp/celebrity/src/commonMain/kotlin/com/alekseivinogradov/anoti/celebrity/kmp/impl/domain/coroutinecontext/CoroutineContextProviderBase.kt)
- `CoroutineContextProviderDefaultImpl` is the app's binding. Its callback logs the throwable and
  shows the unknown error message.
- `CoroutineContextProviderBareImpl` only logs, for code that must not show UI. As found, only a
  test uses it.
- The `*CoroutineContext` members carry this handler, except `workManagerCoroutineContext`,
  which carries an empty one; dispatchers carry none. Which job each context carries, and the
  contexts built outside the provider, are in
  [concurrency-and-lifecycle.md "Coroutine contexts"](concurrency-and-lifecycle.md#coroutine-contexts).
- Which dispatcher and which scope owner to use is owned by
  [concurrency-and-lifecycle.md](concurrency-and-lifecycle.md).

## Catching

- Code that catches everything rethrows `CancellationException` first, then classifies the rest.
  Example: [SafeApiImpl](../../core-kmp/network/src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/SafeApiImpl.kt)
- A catch-all sits where classifying failures is the code's purpose: `SafeApi`, the paginator, a
  background pass. Each carries `@Suppress("TooGenericExceptionCaught")` and a comment saying so.
- A value from outside the app or from saved state that may fail to decode is read inside
  `runCatching { }.getOrNull()`, so any failure becomes "nothing". It carries no suppression.
  Example:
  [NavRootDeepLink](../../core-kmp/navigation/src/commonMain/kotlin/com/alekseivinogradov/anoti/navigation/kmp/NavRootDeepLink.kt),
  [SaveableStateCodec](../../main/src/iosMain/kotlin/com/alekseivinogradov/anoti/main/impl/presentation/savedstate/SaveableStateCodec.kt).
- Code inside the app fails fast on a broken assumption; see [principles.md](principles.md).
  Input from outside the app is handled in
  [security-and-privacy.md](security-and-privacy.md).
- Log lines, failures included, follow [logging.md](../rules/logging.md).
