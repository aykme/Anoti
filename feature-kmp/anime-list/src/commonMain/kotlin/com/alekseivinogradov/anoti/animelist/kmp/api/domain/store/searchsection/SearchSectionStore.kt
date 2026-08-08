package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.searchsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
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
        data object LoadNextPage : Intent
        data class ChangeSearchText(val searchText: String) : Intent
        data class EpisodesInfoClick(val id: AnimeId) : Intent
    }

    sealed interface Label {
        data object ResetListPositionAfterUpdate : Label
    }

    sealed interface Action

    sealed interface Message {
        data class ChangeSearchText(val searchText: String) : Message
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoIds: Set<AnimeId>
        ) : Message

        data class UpdateAnimeDetails(val animeDetails: AnimeDetails) : Message
    }
}
