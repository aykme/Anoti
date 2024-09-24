package com.alekseivinogradov.anime_list.api.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionContentDomain
import com.arkivanov.mvikotlin.core.store.Store

interface OngoingSectionStore
    : Store<OngoingSectionStore.Intent, OngoingSectionStore.State, OngoingSectionStore.Label> {
    data class State(
        val sectionContent: SectionContentDomain = SectionContentDomain()
    )

    sealed interface Intent {
        data object OpenSection : Intent
        data object UpdateSection : Intent
        data class EpisodesInfoClick(val itemIndex: Int) : Intent
    }

    sealed interface Label

    sealed interface Action {
        data object InitSection : Action
    }

    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
    }
}