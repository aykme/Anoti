package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

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
