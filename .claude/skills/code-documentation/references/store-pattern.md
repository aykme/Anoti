# Worked examples: Store-shaped modules, both variants

Two real modules from this project, showing the two ways a Store-shaped module can turn out —
see `SKILL.md`'s "Store-shaped modules" section for how to tell which one you're looking at
before you start.

## Variant A: Store alone — `core-kmp/anime-database`

An MVIKotlin `Store` over a local database. The DI graph behind it has a repository, six
usecases, an executor, a reducer and a store factory — none of that belongs in the README once
you notice the shape. No `View`/`Controller` lives in this module at all; consumers (e.g.
`AnimeListController`, which lives in a *different* module) just get the Store handed to them
by DI and subscribe to it directly.

### The Store's own KDoc

Every case gets its own doc comment, right on the type it documents — this is what a reader
follows from the README's single link. Documentation doesn't stop at the Store's own types: any
parameter or property whose type is itself project-defined gets followed and documented too,
all the way down. This includes `Action`/`Message` — a class-level "this is internal" note is
not a substitute for documenting each case; see `SKILL.md`'s dedicated section on this before
assuming a one-liner is enough.

```kotlin
/**
 * MVI store for the local anime database — the source of truth every feature reads saved anime
 * state from and mutates it through.
 */
interface AnimeDatabaseStore
    : Store<AnimeDatabaseStore.Intent, AnimeDatabaseStore.State, AnimeDatabaseStore.Label> {

    /** Current snapshot of the saved anime list. */
    data class State(
        /** All anime currently saved in the local database. */
        val animeDatabaseItems: List<AnimeDbDomain> = listOf()
    )

    /** Actions a caller can dispatch via [accept]. */
    sealed interface Intent {
        /**
         * Adds an item to the database.
         *
         * @param animeDatabaseItem the item to insert.
         */
        data class InsertAnimeDatabaseItem(val animeDatabaseItem: AnimeDbDomain) : Intent

        /**
         * Removes an item from the database.
         *
         * @param id id of the item to remove.
         */
        data class DeleteAnimeDatabaseItem(val id: AnimeId) : Intent

        /** Clears the "new episode" flag on every item. */
        data object ResetAllItemsNewEpisodeStatus : Intent

        /**
         * Sets the "new episode" flag on one item.
         *
         * @param isNewEpisode the flag's new value.
         * @param id id of the item to update.
         */
        data class ChangeItemNewEpisodeStatus(
            val isNewEpisode: Boolean,
            val id: AnimeId
        ) : Intent

        /**
         * Replaces an existing item with a new version of itself.
         *
         * @param animeDatabaseItem the item's new state; matched to the stored item by its id.
         */
        data class UpdateAnimeDatabaseItem(val animeDatabaseItem: AnimeDbDomain) : Intent
    }

    /** One-off events the store publishes for callers to react to. */
    sealed interface Label {
        /** Published once [Intent.ResetAllItemsNewEpisodeStatus] has completed. */
        data object ResetAllItemsNewEpisodeStatusWasFinished : Label
    }

    /** Internal bootstrap signal consumed by the store's executor — callers never dispatch this. */
    sealed interface Action {
        /** Triggers the executor's subscription to the underlying repository on store creation. */
        data object SubscribeToDatabase : Action
    }

    /** Internal reducer input produced by the executor — callers never dispatch this. */
    sealed interface Message {
        /**
         * Replaces [State.animeDatabaseItems] wholesale.
         *
         * @param animeDatabaseItems the full, up-to-date list of saved anime.
         */
        data class UpdateAnimeDatabaseItems(val animeDatabaseItems: List<AnimeDbDomain>) : Message
    }
}
```

Note `Action`/`Message` are documented too, but their KDoc says outright that callers don't
touch them — that's the difference between "internal, but worth a reader understanding the
whole flow" and "part of the public contract."

### Following the chain past the Store

`State`/`Intent`/`Message` all expose `AnimeDbDomain`. That type gets documented in turn, field
by field — and so does anything *it* exposes (`AnimeId`, `ReleaseStatusDb`):

```kotlin
/**
 * A saved anime entry, as read from and written to [AnimeDatabaseStore].
 * Not the database entity itself, just the mediator type the store/usecases operate on.
 *
 * @param id id of the anime.
 * @param imageUrl cover image URL, or null if unknown.
 * @param name display name of the anime.
 * @param episodesAired number of episodes aired so far, or null if unknown.
 * @param episodesTotal total number of episodes, or null if not yet announced.
 * @param nextEpisodeAt air date/time of the next episode, or null if none is scheduled.
 * @param airedOn date the anime started airing, or null if unknown.
 * @param releasedOn date the anime finished airing, or null if it hasn't finished.
 * @param score community score, or null if unavailable.
 * @param releaseStatus current release status.
 * @param episodesViewed number of episodes the user has watched.
 * @param isNewEpisode whether a new episode has aired since the user last checked.
 */
data class AnimeDbDomain(
    val id: AnimeId,
    val imageUrl: String?,
    val name: String,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val nextEpisodeAt: String?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: Float?,
    val releaseStatus: ReleaseStatusDb,
    val episodesViewed: Int,
    val isNewEpisode: Boolean
)

/** Id of an anime. */
typealias AnimeId = Int

/** Release status of a saved [AnimeDbDomain]. */
enum class ReleaseStatusDb {
    /** Status could not be determined from the source data. */
    UNKNOWN,
    ONGOING,
    ANNOUNCED,
    RELEASED
}
```

`String`, `Int`, `Float`, `Boolean`, `List<T>` are stdlib — the chain stops there.

### The resulting README

Everything behind the Store — the repository, the six usecase interfaces, `AnimeDatabaseUsecases`,
and every `*Impl`/`Executor`/`Reducer`/`Factory` class — is left out. A consumer never imports
any of them; they only ever get an `AnimeDatabaseStore` handed to them by DI. The domain model
(`AnimeDbDomain`, `ReleaseStatusDb`) doesn't get its own bullet either — a reader reaches it by
following `AnimeDatabaseStore`'s own KDoc, not by scanning the README. "How to use it" is prose,
not a code block — same reasoning as Variant B below: a hand-written `AnimeListController`
snippet is one more place that can drift from the real caller if the Store's shape changes:

```markdown
Local anime database for the app — an MVI store over the user's saved anime (subscriptions,
episode progress, "new episode" flags).

## Entities

- [AnimeDatabaseStore](src/commonMain/kotlin/.../api/domain/store/AnimeDatabaseStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.

## How to include it

- Gradle: `implementation(project(":core-kmp:anime-database"))`
- The `AnimeDatabaseStore` instance is provided via this module's own androidMain Dagger setup —
  inject it, don't construct it yourself.

## How to use it

Subscribe to `AnimeDatabaseStore.states`/`labels` and call `accept(Intent)` to read and mutate
the saved anime list — see the Store's own KDoc for what each `Intent`/`Label`/`State` field
means.
```

~15 lines instead of the 230+ you get from walking the DI graph and describing every class you
find along the way.

## Variant B: Store + View + Controller — `feature-kmp/bottom-navigation-bar`

Same MVIKotlin shape, but this module *also* defines `BottomNavigationBarView` (an `MviView`
contract) and `BottomNavigationBarController` (wires the store to the view). The platform
module (`main`) implements the view directly —
`class BottomNavigationBarViewImpl(...) : BottomNavigationBarView` — and constructs the
controller directly in `MainActivity`, with no `@Provides` in between for either. That's the
signal this is Variant B, not Variant A: a real consumer reaches for all three types, not just
the Store.

### The Store's own KDoc

Documented exactly like Variant A — every `State`/`Intent`/`Label`/`Action`/`Message` case gets
its own line, including `Message`, which is easy to shortchange with just the class-level note:

```kotlin
/**
 * The store for the bottom navigation bar.
 */
interface BottomNavigationBarStore : Store<
        BottomNavigationBarStore.Intent,
        BottomNavigationBarStore.State,
        BottomNavigationBarStore.Label
        > {

    /**
     * @param selectedSection currently selected section.
     * @param favoritesBadgeNumber number shown on the favorites tab's badge.
     */
    data class State(
        val selectedSection: SectionDomain = SectionDomain.MAIN,
        val favoritesBadgeNumber: Int = 0
    )

    /** Actions a caller can dispatch via [accept]. */
    sealed interface Intent {
        /** External navigation moved to [selectedSection]; sync the bar's selection. */
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Intent

        /** Update the favorites tab's badge to [favoritesBadgeNumber]. */
        data class UpdateFavoritesBadgeNumber(val favoritesBadgeNumber: Int) : Intent

        /** The user tapped the main tab. */
        data object MainSectionClick : Intent

        /** The user tapped the favorites tab. */
        data object FavoritesSectionClick : Intent
    }

    /** One-off events the store publishes for callers to react to. */
    sealed interface Label {
        /** Navigate to the main section. */
        data object NavigateToMain : Label

        /** Navigate to the favorites section. */
        data object NavigateToFavorites : Label
    }

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Action

    /** Internal executor plumbing; a consumer never dispatches this. */
    sealed interface Message {
        /**
         * Replaces [State.selectedSection].
         *
         * @param selectedSection the newly selected section.
         */
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Message

        /**
         * Replaces [State.favoritesBadgeNumber].
         *
         * @param favoritesBadgeNumber the badge's new number.
         */
        data class UpdateFavoritesBadgeNumber(val favoritesBadgeNumber: Int) : Message
    }
}
```

### The resulting README

Three entities this time, and "how to use it" is prose, not a fabricated code block — the real
wiring already lives in `main`'s `BottomNavigationBarViewImpl`/`MainActivity`, and restating it
as invented-but-plausible-looking Kotlin risks drifting from what those files actually do:

```markdown
The app's bottom navigation bar: an MVI store tracking the selected section and the favorites
badge count.

## Entities

- [BottomNavigationBarStore](src/commonMain/kotlin/.../api/domain/store/BottomNavigationBarStore.kt) —
  the store. `State`/`Intent`/`Label` are documented on the type itself.
- [BottomNavigationBarView](src/commonMain/kotlin/.../impl/presentation/BottomNavigationBarView.kt) —
  the view contract the platform layer implements to render the store's state.
- [BottomNavigationBarController](src/commonMain/kotlin/.../impl/presentation/BottomNavigationBarController.kt) —
  wires the store to its view and to `AnimeDatabaseStore`.

## How to include it

- Gradle: `implementation(project(":feature-kmp:bottom-navigation-bar"))`
- `BottomNavigationBarStore` is provided via `main`'s Dagger setup — inject it, don't construct
  it yourself. `BottomNavigationBarView` has no DI wiring; the consumer implements it directly
  (see `main`'s `BottomNavigationBarViewImpl`). `BottomNavigationBarController` has no DI wiring
  either; construct it directly with the store and lifecycle.

## How to use it

Implement `BottomNavigationBarView` (a `BottomNavigationBarViewImpl`): render `UiModel` in
`render()`, call `dispatch(Intent)` from the relevant UI callbacks (tab clicks), and handle
navigation in `handle(Label)`. On the screen hosting the bar, construct
`BottomNavigationBarController` with the store, `AnimeDatabaseStore`, and the screen's lifecycle,
then call `controller.onViewCreated(viewImpl, viewLifecycle)`.
```

Compare this to what an earlier draft of this same README looked like — it included a full
`BottomNavigationBarViewImpl`/`MainActivity` code sample built from scratch to illustrate the
pattern. It got cut for two reasons once reviewed: it was long enough to feel like the README
had grown an essay again, and it wasn't real code — a handwritten approximation of what
`main`'s actual `BottomNavigationBarViewImpl` does, which is exactly the kind of second source
of truth this whole skill exists to avoid.
