package com.alekseivinogradov.anime_list.impl.domain.store.upper_menu

import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class UpperMenuReducerImpl : Reducer<UpperMenuStore.State, UpperMenuStore.Message> {

    override fun UpperMenuStore.State.reduce(msg: UpperMenuStore.Message): UpperMenuStore.State {
        return when (msg) {
            is UpperMenuStore.Message.ChangeSelectedSection -> copy(
                selectedSection = msg.selectedSection
            )

            is UpperMenuStore.Message.ChangeSearch -> copy(
                search = msg.search
            )
        }
    }
}