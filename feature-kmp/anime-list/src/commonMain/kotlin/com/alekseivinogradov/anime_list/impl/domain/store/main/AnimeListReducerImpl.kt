package com.alekseivinogradov.anime_list.impl.domain.store.main

import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class AnimeListReducerImpl :
    Reducer<AnimeListMainStore.State, AnimeListMainStore.Message> {

    override fun AnimeListMainStore.State.reduce(
        msg: AnimeListMainStore.Message
    ): AnimeListMainStore.State {
        return when (msg) {
            is AnimeListMainStore.Message.ChangeSelectedSection -> copy(
                selectedSection = msg.selectedSection
            )

            is AnimeListMainStore.Message.ChangeSearch -> copy(
                search = msg.search
            )

            is AnimeListMainStore.Message.ChangeOngoingContentType -> copy(
                ongoingContent = ongoingContent.copy(
                    contentType = msg.contentType
                )
            )

            is AnimeListMainStore.Message.UpdateOngoingListItems -> copy(
                ongoingContent = ongoingContent.copy(
                    listItems = msg.listItems
                )
            )

            is AnimeListMainStore.Message.ChangeAnnouncedContentType -> copy(
                announcedContent = announcedContent.copy(
                    contentType = msg.contentType
                )
            )

            is AnimeListMainStore.Message.UpdateAnnouncedListItems -> copy(
                announcedContent = announcedContent.copy(
                    listItems = msg.listItems
                )
            )

            is AnimeListMainStore.Message.ChangeSearchContentType -> copy(
                searchContent = searchContent.copy(
                    contentType = msg.contentType
                )
            )

            is AnimeListMainStore.Message.UpdateSearchListItems -> copy(
                searchContent = searchContent.copy(
                    listItems = msg.listItems
                )
            )

            is AnimeListMainStore.Message.UpdateEnabledNotificationIds -> copy(
                enabledNotificationIds = msg.enabledNotificationIds
            )
        }
    }
}
