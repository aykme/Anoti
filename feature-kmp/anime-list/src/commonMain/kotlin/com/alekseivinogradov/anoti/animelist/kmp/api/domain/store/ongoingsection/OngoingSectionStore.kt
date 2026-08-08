package com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.ongoingsection

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.AnimeDetails
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.arkivanov.mvikotlin.core.store.Store

interface OngoingSectionStore
    : Store<OngoingSectionStore.Intent, OngoingSectionStore.State, OngoingSectionStore.Label> {

    data class State(
        val sectionContent: SectionContentDomain = SectionContentDomain()
    )

    sealed interface Intent {
        data object OpenSection : Intent
        data object UpdateSection : Intent
        data object LoadNextPage : Intent
        data class EpisodesInfoClick(val id: AnimeId) : Intent
    }

    sealed interface Label {
        data object ResetListPositionAfterUpdate : Label
    }

    sealed interface Action {
        data object InitSection : Action
    }

    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledExtraEpisodesInfoIds(
            val enabledExtraEpisodesInfoIds: Set<AnimeId>
        ) : Message

        data class UpdateAnimeDetails(val animeDetails: AnimeDetails) : Message
    }
}
