package com.alekseivinogradov.anime_list.api.domain.store.announced_section

import app.cash.paging.PagingData
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionContentDomain
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

interface AnnouncedSectionStore : Store<
        AnnouncedSectionStore.Intent,
        AnnouncedSectionStore.State,
        AnnouncedSectionStore.Label
        > {
    data class State(
        val sectionContent: SectionContentDomain = SectionContentDomain()
    )

    sealed interface Intent {
        data object OpenSection : Intent
        data object UpdateSection : Intent
        data class EpisodesInfoClick(val listItem: ListItemDomain) : Intent
    }

    sealed interface Label {
        data object ResetListPositionAfterUpdate : Label
    }

    sealed interface Action

    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: PagingData<ListItemDomain>) : Message
        data class UpdateEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoIds: Set<AnimeId>
        ) : Message
    }
}
