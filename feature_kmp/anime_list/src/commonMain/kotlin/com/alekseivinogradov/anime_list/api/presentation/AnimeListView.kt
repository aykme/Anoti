package com.alekseivinogradov.anime_list.api.presentation

import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeListView : MviView<AnimeListView.UiModel, AnimeListView.UiEvent> {

    data class UiModel(
        val selectedSection: SectionUi = SectionUi.ONGOINGS,
        val search: SearchUi = SearchUi.HIDEN,
        val contentType: ContentTypeUi = ContentTypeUi.LOADING,
        val ongoingListItems: List<ListItemUi> = listOf(),
        val announcedListItems: List<ListItemUi> = listOf(),
        val searchListItems: List<ListItemUi> = listOf()
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
        data class ContentTypeChange(val contentType: ContentTypeUi) : UiEvent
    }
}