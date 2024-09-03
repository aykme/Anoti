package com.alekseivinogradov.anime_list.api.presentation.mapper.store

import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView

internal fun mapUiEventToSectionContentIntent(uiEvent: AnimeListView.UiEvent)
        : SectionContentStore.Intent? {
    return when (uiEvent) {
        AnimeListView.UiEvent.UpdateAnnouncedSection,
        AnimeListView.UiEvent.UpdateOngoingsSection,
        AnimeListView.UiEvent.UpdateSearchSection -> SectionContentStore.Intent.UpdateSection

        is AnimeListView.UiEvent.EpisodesInfoClick -> SectionContentStore.Intent.EpisodesInfoClick(
            itemIndex = uiEvent.itemIndex
        )

        is AnimeListView.UiEvent.NotificationClick -> SectionContentStore.Intent.NotificationClick(
            itemIndex = uiEvent.itemIndex
        )

        AnimeListView.UiEvent.OngoingsSectionClick,
        AnimeListView.UiEvent.AnnouncedSectionClick,
        AnimeListView.UiEvent.SearchSectionClick,
        AnimeListView.UiEvent.CancelSearchClick,
        is AnimeListView.UiEvent.SearchTextChange -> null

    }
}