package com.alekseivinogradov.anime_database.api.domain.store

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.arkivanov.mvikotlin.core.store.Store

interface AnimeDatabaseStore
    : Store<AnimeDatabaseStore.Intent, AnimeDatabaseStore.State, AnimeDatabaseStore.Label> {

    data class State(
        val animeDatabaseItems: List<AnimeDbDomain> = listOf()
    )

    sealed interface Intent {
        data class InsertAnimeDatabaseItem(val animeDatabaseItem: AnimeDbDomain) : Intent
        data class DeleteAnimeDatabaseItem(val id: AnimeId) : Intent
        data object ResetAllItemsNewEpisodeStatus : Intent
        data class ChangeItemNewEpisodeStatus(
            val isNewEpisode: Boolean,
            val id: AnimeId
        ) : Intent

        data class UpdateAnimeDatabaseItem(val animeDatabaseItem: AnimeDbDomain) : Intent
    }

    sealed interface Label {
        data object ResetAllItemsNewEpisodeStatusWasFinished : Label
    }

    sealed interface Action {
        data object SubscribeToDatabase : Action
    }

    sealed interface Message {
        data class UpdateAnimeDatabaseItems(val animeDatabaseItems: List<AnimeDbDomain>) : Message
    }
}