package com.alekseivinogradov.anime_list.api.presentation

import com.alekseivinogradov.anime_list.api.model.UiContentType
import com.alekseivinogradov.anime_list.api.model.UiSearch
import com.alekseivinogradov.anime_list.api.model.UiSection
import com.alekseivinogradov.anime_list.api.model.list_content.UiListItem
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeListView : MviView<AnimeListView.UiModel, AnimeListView.UiEvent> {

    data class UiModel(
        val selectedSection: UiSection = UiSection.ONGOINGS,
        val search: UiSearch = UiSearch.HIDEN,
        val contentType: UiContentType = UiContentType.LOADING,
        val ongoingListItems: List<UiListItem> = listOf(),
        val announcedListItems: List<UiListItem> = listOf(),
        val searchListItems: List<UiListItem> = listOf()
    )

    sealed interface UiEvent {
        data object UpdateOngoingsSection : UiEvent
        data object UpdateAnnouncedSection : UiEvent
        data object UpdateSearchSection : UiEvent
        data object OngoingsSectionClick : UiEvent
        data object AnnouncedSectionClick : UiEvent
        data object SearchSectionClick : UiEvent
        data object CancelSearchClick : UiEvent
        data class SearchTextChange(val text: String) : UiEvent
        data class EpisodesInfoClick(val itemIndex: Int) : UiEvent
        data class NotificationClick(val itemIndex: Int) : UiEvent
    }
}