package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class SearchSectionReducerImpl :
    Reducer<SearchSectionStore.State, SearchSectionStore.Message> {

    override fun SearchSectionStore.State.reduce(msg: SearchSectionStore.Message):
            SearchSectionStore.State {
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
        }
    }
}
