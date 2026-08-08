KMP network layer: a small, DI-framework-agnostic layer for making safe HTTP calls with Ktor and
classifying their outcome uniformly across the app.

## Why this module exists

Every feature module that talks to a backend needs the same two things: a way to turn "the call
might throw" into "the call returns a typed result", and a shared `HttpClient` configuration. This
module is fully in `commonMain` — nothing here is platform-specific, so it compiles and runs the
same way on Android and iOS. Only the choice of Ktor engine (OkHttp, Darwin, ...) is left to the
caller.

This module has no dependency on any DI framework. Callers construct
[SafeApiImpl](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/SafeApiImpl.kt)
and the `HttpClient` themselves (typically from their platform's DI setup) and pass them down.

## Entities

- [SafeApi](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/data/SafeApi.kt) —
  the contract every network call should go through. See its KDoc for the retry/classification
  rules.
- [CallResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/CallResult.kt) —
  the result type `SafeApi.call` returns: `Success`, or one of the `Failure` subtypes
  (`HttpError`, `NetworkError`, `OtherError`). See its KDoc for what each variant means and when
  to use the `Failure` marker instead of listing all three.
- [SafeApiImpl](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/SafeApiImpl.kt) —
  the real, Ktor-based `SafeApi` implementation used in production.
- [SafeApiFake](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/fake/SafeApiFake.kt) —
  a test/preview `SafeApi` double.
- [createHttpClient](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/HttpClientFactory.kt) —
  builds the `HttpClient` every API service should use (JSON content negotiation,
  `expectSuccess = true`). Takes an already-constructed `HttpClientEngine`; this module does not
  select or depend on a specific engine.
- [DesiredCallResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/test/DesiredCallResult.kt) —
  drives fake service implementations (`SUCCESS`/`HTTP_ERROR`/`OTHER_ERROR`) in tests.
- [SHIKIMORI_BASE_URL](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/Const.kt) —
  base URL of the backend the app currently talks to.

## How to use it

Building the client and wrapping a call:

```kotlin
val httpClient = createHttpClient(engine = /* platform HttpClientEngine, e.g. OkHttp.create() */)
val safeApi: SafeApi = SafeApiImpl(maxAttempt = 3, attemptDelay = 2500.milliseconds)

suspend fun fetchSomething(): CallResult<SomeResponse> = safeApi.call {
    httpClient.get("$SHIKIMORI_BASE_URL/api/some/path").body()
}
```

Handling the result — match on `Failure` when the caller doesn't need to distinguish *why* it
failed, otherwise match on the specific subtype (e.g. to show a different message for
`NetworkError`/`HttpError` — a genuine connectivity problem — versus `OtherError`, which is not):

```kotlin
when (val result = fetchSomething()) {
    is CallResult.Success -> handle(result.value)
    is CallResult.HttpError,
    is CallResult.NetworkError -> toastProvider.makeConnectionErrorToast()
    is CallResult.OtherError -> toastProvider.makeUnknownErrorToast()
}
```

## Current rollout state — read before wiring `SafeApiImpl` in anywhere new

`SafeApiImpl.call`'s failure classification only recognizes Ktor's own exception types
(`ResponseException`, `kotlinx.io.IOException`). It does **not** understand `retrofit2.HttpException`
or any other HTTP client's exceptions.

As of this writing, `core-platform/network`'s DI still wires the old Retrofit-aware
`SafeApiImpl` (`com.alekseivinogradov.anoti.network.platform.impl.data.SafeApiImpl`), because the
only real API service in the app (`ShikimoriApiServicePlatform`, in `feature-platform/anime-base`)
still makes its calls through Retrofit. Swapping the DI-provided `SafeApi` to this module's
`SafeApiImpl` before that service is migrated to Ktor would silently misclassify every real HTTP
error as `CallResult.OtherError` (and stop retrying 5xx responses) instead of breaking loudly — do
not do that swap until the API service itself calls through a Ktor `HttpClient`.

## What's intentionally not here

- **No DI wiring.** `core-platform/network` provides `SafeApi`/`HttpClient` through Dagger for the
  Android app; this module stays framework-agnostic so it can be consumed the same way from any DI
  setup (including a future KMP-native one).
- **No engine selection.** `createHttpClient` takes an `HttpClientEngine`; picking OkHttp, Darwin,
  etc. is the caller's responsibility.
