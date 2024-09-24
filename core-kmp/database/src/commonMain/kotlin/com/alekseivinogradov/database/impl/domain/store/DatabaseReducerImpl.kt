package com.alekseivinogradov.database.impl.domain.store

import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class DatabaseReducerImpl :
    Reducer<DatabaseStore.State, DatabaseStore.Message> {

    override fun DatabaseStore.State.reduce(msg: DatabaseStore.Message):
            DatabaseStore.State {
        return when (msg) {
            is DatabaseStore.Message.UpdateAnimeDatabaseItems -> copy(
                animeDatabaseItems = msg.animeDatabaseItems
            )
        }
    }
}
