package com.alekseivinogradov.anime_list.api.domain.store.ongoing_section

import com.alekseivinogradov.anime_list.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.arkivanov.mvikotlin.core.store.Store

interface OngoingSectionStore
    : Store<OngoingSectionStore.Intent, OngoingSectionStore.State, OngoingSectionStore.Label> {
    data class State(
        val contentType: ContentTypeDomain = ContentTypeDomain.LOADING,
        val listItems: List<ListItemDomain> = listOf(),
        val enabledNotificationIds: Set<AnimeId> = setOf()
    )

    sealed interface Intent {
        data object InitSection : Intent
        data object UpdateSection : Intent
        data class EpisodesInfoClick(val itemIndex: Int) : Intent
        data class NotificationClick(val itemIndex: Int) : Intent
    }

    sealed interface Label

    sealed interface Action {
        data object SubscribeToDatabase : Action
    }

    sealed interface Message {
        data class ChangeContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Message
    }
}