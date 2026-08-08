Shared network layer: safe API calls with retries, and a common `HttpClient` setup, for the whole
app.

## Entities

- [SafeApi](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/data/SafeApi.kt) —
  safe API calls with retries.
- [CallResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/CallResult.kt) —
  outcome of a call made through `SafeApi`: success or a typed failure.
- [createHttpClient](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/HttpClientFactory.kt) —
  creates the `HttpClient` API services should use.
- [SHIKIMORI_BASE_URL](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/Const.kt) —
  base Shikimori URL of the backend.

## How to use it

```kotlin
val httpClient = createHttpClient(engine = /* e.g. OkHttp.create() */)
val safeApi: SafeApi = SafeApiImpl(maxAttempt = 3, attemptDelay = 2500.milliseconds)

suspend fun fetchSomething(): CallResult<SomeResponse> = safeApi.call {
    httpClient.get("$SHIKIMORI_BASE_URL/api/some/path").body()
}
```

## What's intentionally not here

- No DI wiring — callers get `SafeApi`/`HttpClient` from their own DI setup.
- No engine selection — `createHttpClient` takes an already-built `HttpClientEngine`.
