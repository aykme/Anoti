package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.store.searchsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection.SearchSectionStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class SearchSectionReducerImpl :
    Reducer<SearchSectionStore.State, SearchSectionStore.Message> {

    override fun SearchSectionStore.State.reduce(msg: SearchSectionStore.Message): SearchSectionStore.State {
        return when (msg) {
            is SearchSectionStore.Message.ChangeContentType -> copy(
                sectionContent = sectionContent.copy(
                    contentType = msg.contentType
                )
            )

            is SearchSectionStore.Message.UpdateListItems -> copy(
                sectionContent = sectionContent.copy(
                    listItems = msg.listItems
                )
            )

            is SearchSectionStore.Message.ChangeSearchText -> copy(
                searchText = msg.searchText
            )

            is SearchSectionStore.Message.UpdateEnabledExtraEpisodesInfoIds -> copy(
                sectionContent = sectionContent.copy(
                    enabledExtraEpisodesInfoIds = msg.enabledExtraEpisodesInfoIds
                )
            )

            is SearchSectionStore.Message.UpdateAnimeDetails -> copy(
                sectionContent = sectionContent.copy(
                    animeDetails = msg.animeDetails
                )
            )

            is SearchSectionStore.Message.RestoreSection -> copy(
                sectionContent = sectionContent.copy(
                    enabledExtraEpisodesInfoIds = msg.enabledExtraEpisodesInfoIds,
                    animeDetails = AnimeDetails(nextEpisodesInfo = msg.nextEpisodesInfo)
                ),
                restoreTargetItemCount = msg.itemCount.takeIf { it > 0 }
            )

            SearchSectionStore.Message.ClearRestoreTargetItemCount -> copy(
                restoreTargetItemCount = null
            )
        }
    }
}
