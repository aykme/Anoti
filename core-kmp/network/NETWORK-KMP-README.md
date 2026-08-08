Shared network layer: safe API calls with retries, and a common `HttpClient` setup, for the whole
app.

## Entities

- [SafeApi](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/data/SafeApi.kt)
- [CallResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/CallResult.kt)
- [createHttpClient](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/HttpClientFactory.kt) —
  creates an `HttpClient`.
- [SHIKIMORI_BASE_URL](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/Const.kt)

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
