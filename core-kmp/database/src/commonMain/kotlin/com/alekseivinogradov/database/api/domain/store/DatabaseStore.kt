package com.alekseivinogradov.database.api.domain.store

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.database.api.domain.model.AnimeDb
import com.arkivanov.mvikotlin.core.store.Store

interface DatabaseStore
    : Store<DatabaseStore.Intent, DatabaseStore.State, DatabaseStore.Label> {

    data class State(
        val animeDatabaseItems: List<AnimeDb> = listOf()
    )

    sealed interface Intent {
        data class InsertAnimeDatabaseItem(val animeDatabaseItem: AnimeDb) : Intent
        data class DeleteAnimeDatabaseItem(val id: AnimeId) : Intent
        data object ResetAllItemsNewEpisodeStatus : Intent
        data class ChangeItemNewEpisodeStatus(
            val isNewEpisode: Boolean,
            val id: AnimeId
        ) : Intent

        data class UpdateAnimeDatabaseItem(val animeDatabaseItem: AnimeDb) : Intent
    }

    sealed interface Label

    sealed interface Action {
        data object SubscribeToDatabase : Action
    }

    sealed interface Message {
        data class UpdateAnimeDatabaseItems(val animeDatabaseItems: List<AnimeDb>) : Message
    }
}