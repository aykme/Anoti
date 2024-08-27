package com.alekseivinogradov.anime_list.impl.domain.store.upper_menu

import com.alekseivinogradov.anime_list.impl.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.impl.domain.model.SectionDomain
import com.arkivanov.mvikotlin.core.store.Reducer

internal class UpperMenuReducer : Reducer<UpperMenuStore.State, UpperMenuReducer.Message> {

    internal sealed interface Message {
        data class ChangeSelectedSection(val selectedSection: SectionDomain) : Message
        data class ChangeSearch(val search: SearchDomain) : Message
    }

    override fun UpperMenuStore.State.reduce(msg: Message): UpperMenuStore.State {
        return when (msg) {
            is Message.ChangeSelectedSection -> copy(
                selectedSection = msg.selectedSection
            )

            is Message.ChangeSearch -> copy(
                search = msg.search
            )
        }
    }
}