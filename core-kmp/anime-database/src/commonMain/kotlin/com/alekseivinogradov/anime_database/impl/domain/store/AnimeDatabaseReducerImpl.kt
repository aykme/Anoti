package com.alekseivinogradov.anime_database.impl.domain.store

import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class AnimeDatabaseReducerImpl :
    Reducer<AnimeDatabaseStore.State, AnimeDatabaseStore.Message> {

    override fun AnimeDatabaseStore.State.reduce(msg: AnimeDatabaseStore.Message):
            AnimeDatabaseStore.State {
        return when (msg) {
            is AnimeDatabaseStore.Message.UpdateAnimeDatabaseItems -> copy(
                animeDatabaseItems = msg.animeDatabaseItems
            )
        }
    }
}
