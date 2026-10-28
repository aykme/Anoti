Opens a web page in the system's browser, outside the app.

## Entities

- [OpenWebPageUsecase](src/commonMain/kotlin/com/alekseivinogradov/anoti/webpageopener/kmp/api/domain/usecase/OpenWebPageUsecase.kt) —
  opens a web page in the system's browser.

## How to include it

- Gradle: `implementation(project(":feature-kmp:web-page-opener"))`
- `OpenWebPageUsecase` is provided by this module's per-platform
  `DiWebPageOpenerPlatformComponent` (a separate one on Android and on iOS), mixed into
  `DiAppComponent` on both platforms. It reaches a screen through `DiRootDependencies` and the
  screen's own dependencies — inject it, don't construct it yourself.
- Tests in other modules take `OpenWebPageUsecaseFake` from this module instead. It opens nothing
  and records every address asked for.

## How to use it

```kotlin
// InfoDialogExecutorImpl, on its main context:
scope.launch {
    if (!usecases.openWebPageUsecase.execute(url)) {
        systemMessageProvider.makeUnknownErrorSystemMessage()
    }
}
```
