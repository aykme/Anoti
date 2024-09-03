package com.alekseivinogradov.anime_list.api.domain.store.section_content

import com.alekseivinogradov.anime_list.api.domain.model.section_content.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.arkivanov.mvikotlin.core.store.Store

interface SectionContentStore
    : Store<SectionContentStore.Intent, SectionContentStore.State, SectionContentStore.Label> {

    data class State(
        val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
        val listItems: List<ListItemDomain> = listOf()
    )

    sealed interface Intent {
        data object InitSection : Intent
        data object UpdateSection : Intent
        data class EpisodesInfoClick(val itemIndex: Int) : Intent
        data class NotificationClick(val itemIndex: Int) : Intent
    }

    sealed interface Label

    sealed interface Action

    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
    }
}