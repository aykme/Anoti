Local anime database for the app — an MVI store over the user's saved anime (subscriptions,
episode progress, "new episode" flags).

## Entities

- [AnimeDatabaseStore](src/commonMain/kotlin/com/alekseivinogradov/anoti/animedatabase/kmp/api/domain/store/AnimeDatabaseStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.

## How to include it

- Gradle: `implementation(project(":core-kmp:anime-database"))`
- The `AnimeDatabaseStore` instance is provided via the platform module's Dagger setup
  (`core-platform/anime-database`) — inject it, don't construct it yourself.

## How to use it

```kotlin
class AnimeListController(
    private val animeDatabaseStore: AnimeDatabaseStore,
    // ...
) {
    init {
        animeDatabaseStore.states
            .map { it.animeDatabaseItems }
            .subscribe { items -> /* ... */ }

        animeDatabaseStore.labels
            .subscribe { label ->
                when (label) {
                    AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished -> { /* ... */ }
                }
            }
    }

    fun onEpisodeWatched(id: AnimeId) {
        animeDatabaseStore.accept(
            AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(id = id, isNewEpisode = false)
        )
    }
}
```
