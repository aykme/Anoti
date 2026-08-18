Local anime database for the app — an MVI store over the user's saved anime (subscriptions,
episode progress, "new episode" flags).

## Entities

- [AnimeDatabaseStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/store/AnimeDatabaseStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.

## How to include it

- Gradle: `implementation(project(":core-kmp:anime-database"))`
- The `AnimeDatabaseStore` instance is provided via this module's kotlin-inject-anvil
  contributions (`DiAnimeDatabaseComponent`, `DiAnimeDatabasePlatformComponent`), merged into the
  app-scope graph (`:app`'s `DiAppComponent` on Android,
  [`core-kmp:di`](../di/CORE-KMP-DI-README.md)'s `DiAppComponent` on iOS) — inject it, don't
  construct it yourself. Each screen gets its own instance and disposes it with its own
  lifecycle. The Room-KMP persistence behind it (DAO, entity, database, mapper, repository) and
  the DI wiring both live entirely inside this module.

## How to use it

Subscribe to `AnimeDatabaseStore.states`/`labels` and call `accept(Intent)` to read and mutate
the saved anime list — see the Store's own KDoc for what each `Intent`/`Label`/`State` field
means.
