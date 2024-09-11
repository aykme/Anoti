package com.alekseivinogradov.anime_list.api.domain.store.search_section

import com.alekseivinogradov.anime_list.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.arkivanov.mvikotlin.core.store.Store

interface SearchSectionStore
    : Store<SearchSectionStore.Intent, SearchSectionStore.State, SearchSectionStore.Label> {
    data class State(
        val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
        val searchText: String = "",
        val listItems: List<ListItemDomain> = listOf(),
        val enabledNotificationIds: Set<AnimeId> = setOf()
    )

    sealed interface Intent {
        data object OpenSection : Intent
        data object UpdateSection : Intent
        data class SearchTextChange(val searchText: String) : Intent
        data class EpisodesInfoClick(val itemIndex: Int) : Intent
        data class NotificationClick(val itemIndex: Int) : Intent
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Intent
    }

    sealed interface Label {
        data class EnableNotification(val listItem: ListItemDomain) : Label
        data class DisableNotification(val id: Int) : Label
    }

    sealed interface Action {
        data object InitSection : Action
    }

    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
        data class ChangeSearchText(val searchText: String) : Message
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Message
    }
}