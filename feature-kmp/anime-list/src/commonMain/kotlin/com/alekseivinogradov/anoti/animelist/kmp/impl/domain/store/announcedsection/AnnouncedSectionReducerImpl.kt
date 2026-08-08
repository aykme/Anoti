package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.announcedsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.announcedsection.AnnouncedSectionStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class AnnouncedSectionReducerImpl :
    Reducer<AnnouncedSectionStore.State, AnnouncedSectionStore.Message> {

    override fun AnnouncedSectionStore.State.reduce(
        msg: AnnouncedSectionStore.Message
    ): AnnouncedSectionStore.State {
        return when (msg) {
            is AnnouncedSectionStore.Message.ChangeContentType -> copy(
                sectionContent = sectionContent.copy(
                    contentType = msg.contentType
                )
            )

            is AnnouncedSectionStore.Message.UpdateListItems -> copy(
                sectionContent = sectionContent.copy(
                    listItems = msg.listItems
                )
            )

            is AnnouncedSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds -> copy(
                sectionContent = sectionContent.copy(
                    enabledExtraEpisodesInfoIds = msg.enabledExtraEpisodesInfoIds
                )
            )
        }
    }
}
