package com.alekseivinogradov.anime_list.impl.domain.store.announced_section

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class AnnouncedSectionReducerImpl :
    Reducer<AnnouncedSectionStore.State, AnnouncedSectionStore.Message> {

    override fun AnnouncedSectionStore.State.reduce(msg: AnnouncedSectionStore.Message):
            AnnouncedSectionStore.State {
        return when (msg) {
            is AnnouncedSectionStore.Message.ChangeContentType -> copy(
                contentType = msg.contentType
            )

            is AnnouncedSectionStore.Message.UpdateListItems -> copy(
                listItems = msg.listItems
            )

            is AnnouncedSectionStore.Message.UpdateEnabledNotificationIds -> copy(
                enabledNotificationIds = msg.enabledNotificationIds
            )
        }
    }
}