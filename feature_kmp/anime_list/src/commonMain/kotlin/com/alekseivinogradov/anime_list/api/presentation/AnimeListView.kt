package com.alekseivinogradov.anime_list.api.presentation

import com.alekseivinogradov.anime_list.api.presentation.mapper.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeListView : MviView<UiModel, AnimeListView.UiEvent> {

    sealed interface UiEvent {
        data object UpdateOngoingsSection : UiEvent
        data object UpdateAnnouncedSection : UiEvent
        data object UpdateSearchSection : UiEvent
        data object OngoingsSectionClick : UiEvent
        data object AnnouncedSectionClick : UiEvent
        data object SearchSectionClick : UiEvent
        data class SearchTextChange(val searchText: String) : UiEvent
        data object CancelSearchClick : UiEvent
        data class EpisodesInfoClick(val itemIndex: Int) : UiEvent
        data class NotificationClick(val itemIndex: Int) : UiEvent
    }
}