package com.alekseivinogradov.database.api.domain.store

import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.arkivanov.mvikotlin.core.store.Store

interface DatabaseStore
    : Store<DatabaseStore.Intent, DatabaseStore.State, DatabaseStore.Label> {

    data class State(
        val animeDatabaseItems: List<AnimeDb> = listOf()
    )

    sealed interface Intent {
        data class InsertAnimeDatabaseItem(val animeDatabaseItem: AnimeDb) : Intent
        data class DeleteAnimeDatabaseItem(val id: Int) : Intent
    }

    sealed interface Label

    sealed interface Action {
        data object SubscribeToDatabase : Action
    }

    sealed interface Message {
        data class UpdateAnimeDatabaseItems(val animeDatabaseItems: List<AnimeDb>) : Message
    }
}