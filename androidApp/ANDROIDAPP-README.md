The Android app: the `Application` that builds the app-wide dependency graph, and the APK's
manifest, resources and shrinking rules.

## Entities

- [AnotiApp](src/main/kotlin/com/alekseivinogradov/anoti/impl/presentation/AnotiApp.kt) —
  the Android `Application`.

## How to include it

- Nothing depends on this module. It is the Android application itself.
- The system creates `AnotiApp`, which the manifest names as the application class.

## How to use it

`AnotiApp` builds `DiAppComponent` from [`core-kmp:di-app`](../core-kmp/di-app/CORE-KMP-DI-APP-README.md)
at start. It implements `DiRootComponentHolder` from [`main`](../main/MAIN-README.md), so
`MainActivity` asks it for a new root graph each time it is created.
