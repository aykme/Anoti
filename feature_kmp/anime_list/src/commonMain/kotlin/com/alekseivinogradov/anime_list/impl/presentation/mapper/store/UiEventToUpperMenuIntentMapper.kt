package com.alekseivinogradov.anime_list.impl.presentation.mapper.store

import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.impl.domain.store.upper_menu.UpperMenuStore

internal fun mapUiEventToUpperMenuIntent(uiEvent: AnimeListView.UiEvent): UpperMenuStore.Intent? {
    return when (uiEvent) {
        AnimeListView.UiEvent.OngoingsSectionClick -> UpperMenuStore.Intent.OngoingsSectionClick
        AnimeListView.UiEvent.AnnouncedSectionClick -> UpperMenuStore.Intent.AnnouncedSectionClick
        AnimeListView.UiEvent.SearchSectionClick -> UpperMenuStore.Intent.SearchSectionClick
        AnimeListView.UiEvent.CancelSearchClick -> UpperMenuStore.Intent.CancelSearchClick
        is AnimeListView.UiEvent.SearchTextChange -> UpperMenuStore.Intent.SearchTextChange(
            text = uiEvent.text
        )

        AnimeListView.UiEvent.UpdateAnnouncedSection,
        AnimeListView.UiEvent.UpdateOngoingsSection,
        AnimeListView.UiEvent.UpdateSearchSection,
        is AnimeListView.UiEvent.ContentTypeChange,
        is AnimeListView.UiEvent.EpisodesInfoClick,
        is AnimeListView.UiEvent.NotificationClick,
        -> null
    }
}