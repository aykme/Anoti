package com.alekseivinogradov.anime_list.api.domain.store.search_section

import app.cash.paging.PagingData
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionContentDomain
import com.arkivanov.mvikotlin.core.store.Store

interface SearchSectionStore
    : Store<SearchSectionStore.Intent, SearchSectionStore.State, SearchSectionStore.Label> {
    data class State(
        val searchText: String = "",
        val sectionContent: SectionContentDomain = SectionContentDomain()
    )

    sealed interface Intent {
        data object OpenSection : Intent
        data object UpdateSection : Intent
        data class ChangeSearchText(val searchText: String) : Intent
        data class EpisodesInfoClick(val listItem: ListItemDomain) : Intent
    }

    sealed interface Label {
        data object ResetListPositionAfterUpdate : Label
    }

    sealed interface Action

    sealed interface Message {
        data class ChangeSearchText(val searchText: String) : Message
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: PagingData<ListItemDomain>) : Message
        data class UpdateEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoId: Set<AnimeId>
        ) : Message

        data class UpdateNextEpisodesInfo(val nextEpisodesInfo: Map<AnimeId, String>) : Message
    }
}
