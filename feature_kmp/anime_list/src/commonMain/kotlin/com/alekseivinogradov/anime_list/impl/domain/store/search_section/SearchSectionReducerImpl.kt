package com.alekseivinogradov.anime_list.impl.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.arkivanov.mvikotlin.core.store.Reducer

internal class SearchSectionReducerImpl :
    Reducer<SearchSectionStore.State, SearchSectionStore.Message> {

    override fun SearchSectionStore.State.reduce(msg: SearchSectionStore.Message):
            SearchSectionStore.State {
        return when (msg) {
            is SearchSectionStore.Message.ChangeContentType -> copy(
                contentType = msg.contentType
            )

            is SearchSectionStore.Message.UpdateListItems -> copy(
                listItems = msg.listItems
            )

            is SearchSectionStore.Message.ChangeSearchText -> copy(
                searchText = msg.searchText
            )
        }
    }
}