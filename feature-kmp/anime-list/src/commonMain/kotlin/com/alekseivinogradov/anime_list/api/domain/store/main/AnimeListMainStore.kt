package com.alekseivinogradov.anime_list.api.domain.store.main

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionContentDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionHatDomain
import com.arkivanov.mvikotlin.core.store.Store

interface AnimeListMainStore
    : Store<AnimeListMainStore.Intent, AnimeListMainStore.State, AnimeListMainStore.Label> {
    data class State(
        val selectedSection: SectionHatDomain = SectionHatDomain.ONGOINGS,
        val search: SearchDomain = SearchDomain(),
        val ongoingContent: SectionContentDomain = SectionContentDomain(),
        val announcedContent: SectionContentDomain = SectionContentDomain(),
        val searchContent: SectionContentDomain = SectionContentDomain(),
        val enabledNotificationIds: Set<AnimeId> = setOf()
    )

    sealed interface Intent {
        data object OngoingsSectionClick : Intent
        data object AnnouncedSectionClick : Intent
        data object SearchSectionClick : Intent
        data object CancelSearchClick : Intent
        data class ChangeSearchText(val searchText: String) : Intent
        data object UpdateSection : Intent
        data class UpdateOngoingContent(val content: SectionContentDomain) : Intent
        data class UpdateAnnouncedContent(val content: SectionContentDomain) : Intent
        data class UpdateSearchContent(val content: SectionContentDomain) : Intent
        data class EpisodesInfoClick(val itemIndex: Int) : Intent
        data class NotificationClick(val itemIndex: Int) : Intent
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Intent
    }

    sealed interface Label {
        data object OpenOngoingSection : Label
        data object OpenAnnouncedSection : Label
        data object OpenSearchSection : Label
        data object UpdateOngoingSection : Label
        data object UpdateAnnouncedSection : Label
        data object UpdateSearchSection : Label
        data class ChangeSearchText(val searchText: String) : Label
        data class OngoingEpisodeInfoClick(val itemIndex: Int) : Label
        data class AnnouncedEpisodeInfoClick(val itemIndex: Int) : Label
        data class SearchEpisodeInfoClick(val itemIndex: Int) : Label
        data class EnableNotification(val listItem: ListItemDomain) : Label
        data class DisableNotification(val id: AnimeId) : Label
    }

    sealed interface Action

    sealed interface Message {
        data class ChangeSelectedSection(val selectedSection: SectionHatDomain) : Message
        data class ChangeSearch(val search: SearchDomain) : Message
        data class ChangeOngoingContentType(val contentType: ContentTypeDomain) : Message
        data class ChangeAnnouncedContentType(val contentType: ContentTypeDomain) : Message
        data class ChangeSearchContentType(val contentType: ContentTypeDomain) : Message
        data class UpdateOngoingListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateAnnouncedListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateSearchListItems(val listItems: List<ListItemDomain>) : Message
        data class UpdateEnabledNotificationIds(val enabledNotificationIds: Set<AnimeId>) : Message
    }
}
