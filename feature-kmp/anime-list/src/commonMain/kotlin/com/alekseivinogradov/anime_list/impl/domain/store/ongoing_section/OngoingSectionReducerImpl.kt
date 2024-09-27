package com.alekseivinogradov.anime_list.impl.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class OngoingSectionReducerImpl :
    Reducer<OngoingSectionStore.State, OngoingSectionStore.Message> {

    override fun OngoingSectionStore.State.reduce(
        msg: OngoingSectionStore.Message
    ): OngoingSectionStore.State {
        return when (msg) {
            is OngoingSectionStore.Message.ChangeContentType -> copy(
                sectionContent = sectionContent.copy(
                    contentType = msg.contentType
                )
            )

            is OngoingSectionStore.Message.UpdateListItems -> copy(
                sectionContent = sectionContent.copy(
                    listItems = msg.listItems
                )
            )

            is OngoingSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds -> copy(
                sectionContent = sectionContent.copy(
                    enabledExtraEpisodesInfoIds = msg.enabledExtraEpisodesInfoId
                )
            )
        }
    }
}
