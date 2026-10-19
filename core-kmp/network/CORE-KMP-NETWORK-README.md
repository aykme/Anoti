Shared network layer: safe API calls with retries, a common `HttpClient` setup, and the image
loader, for the whole app.

## Entities

- [SafeApi](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/data/SafeApi.kt) —
  safe API calls with retries.
- [CallResult](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/api/domain/model/CallResult.kt) —
  outcome of a call made through `SafeApi`: success or a typed failure.
- [createHttpClient](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/HttpClientFactory.kt) —
  creates the `HttpClient` API services should use.
- [createImageLoader](src/commonMain/kotlin/com/alekseivinogradov/anoti/network/kmp/impl/data/client/ImageLoaderFactory.kt) —
  creates the app's Coil `ImageLoader`.

## How to include it

- Gradle: `implementation(project(":core-kmp:network"))`
- `SafeApi`/`HttpClient`/`ImageLoader` instances are provided via this module's kotlin-inject
  bindings (`DiNetworkComponent`, `DiNetworkPlatformComponent`), mixed into
  [`core-kmp:di-app`](../di-app/CORE-KMP-DI-APP-README.md)'s `DiAppComponent` on both platforms —
  inject them, don't construct them yourself.
- Each platform's entry installs the `ImageLoader` as Coil's singleton at start-up, so
  `SingletonImageLoader.get` and Coil's composables use it.

## How to use it

```kotlin
suspend fun fetchSomething(): CallResult<SomeResponse> = safeApi.call {
    httpClient.get("$SHIKIMORI_BASE_URL/api/some/path").body()
}
```
