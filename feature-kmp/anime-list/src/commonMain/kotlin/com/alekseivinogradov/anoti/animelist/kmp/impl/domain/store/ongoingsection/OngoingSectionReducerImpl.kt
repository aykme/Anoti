package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection.OngoingSectionStore
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
                    enabledExtraEpisodesInfoIds = msg.enabledExtraEpisodesInfoIds
                )
            )

            is OngoingSectionStore.Message.UpdateAnimeDetails -> copy(
                sectionContent = sectionContent.copy(
                    animeDetails = msg.animeDetails
                )
            )

            is OngoingSectionStore.Message.RestoreSection -> copy(
                sectionContent = sectionContent.copy(
                    enabledExtraEpisodesInfoIds = msg.enabledExtraEpisodesInfoIds,
                    animeDetails = AnimeDetails(nextEpisodesInfo = msg.nextEpisodesInfo)
                ),
                restoreTargetItemCount = msg.itemCount.takeIf { it > 0 }
            )

            OngoingSectionStore.Message.ClearRestoreTargetItemCount -> copy(
                restoreTargetItemCount = null
            )
        }
    }
}
