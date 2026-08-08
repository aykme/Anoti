package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
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
