Shared network layer: safe API calls with retries, and a common `HttpClient` setup, for the whole
app.

## Entities

- [SafeApi](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/data/SafeApi.kt) —
  safe API calls with retries.
- [CallResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/CallResult.kt) —
  outcome of a call made through `SafeApi`: success or a typed failure.
- [createHttpClient](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/HttpClientFactory.kt) —
  creates the `HttpClient` API services should use.

## How to include it

- Gradle: `implementation(project(":core-kmp:network"))`
- `SafeApi`/`HttpClient` instances are provided via this module's kotlin-inject-anvil
  contributions (`NetworkComponent`, `NetworkPlatformComponent`), merged into the app-scope graph
  (`:app`'s `AppGraph` on Android, [`core-kmp:di`](../di/CORE-KMP-DI-README.md)'s `IosAppGraph` on
  iOS) — inject them, don't construct them yourself.

## How to use it

```kotlin
suspend fun fetchSomething(): CallResult<SomeResponse> = safeApi.call {
    httpClient.get("$SHIKIMORI_BASE_URL/api/some/path").body()
}
```
