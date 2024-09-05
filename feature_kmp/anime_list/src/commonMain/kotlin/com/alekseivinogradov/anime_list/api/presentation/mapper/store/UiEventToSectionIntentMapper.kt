package com.alekseivinogradov.anime_list.api.presentation.mapper.store

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView

internal fun mapUiEventToOngoingSectionIntent(uiEvent: AnimeListView.UiEvent)
        : OngoingSectionStore.Intent? {
    return when (uiEvent) {
        AnimeListView.UiEvent.UpdateOngoingsSection ->
            OngoingSectionStore.Intent.UpdateSection

        is AnimeListView.UiEvent.EpisodesInfoClick ->
            OngoingSectionStore.Intent.EpisodesInfoClick(
                itemIndex = uiEvent.itemIndex
            )

        is AnimeListView.UiEvent.NotificationClick ->
            OngoingSectionStore.Intent.NotificationClick(
                itemIndex = uiEvent.itemIndex
            )

        AnimeListView.UiEvent.UpdateAnnouncedSection,
        AnimeListView.UiEvent.UpdateSearchSection,
        AnimeListView.UiEvent.OngoingsSectionClick,
        AnimeListView.UiEvent.AnnouncedSectionClick,
        AnimeListView.UiEvent.SearchSectionClick,
        AnimeListView.UiEvent.CancelSearchClick,
        is AnimeListView.UiEvent.SearchTextChange -> null

    }
}

internal fun mapUiEventToAnnouncedSectionIntent(uiEvent: AnimeListView.UiEvent)
        : AnnouncedSectionStore.Intent? {
    return when (uiEvent) {
        AnimeListView.UiEvent.UpdateAnnouncedSection ->
            AnnouncedSectionStore.Intent.UpdateSection

        is AnimeListView.UiEvent.EpisodesInfoClick ->
            AnnouncedSectionStore.Intent.EpisodesInfoClick(
                itemIndex = uiEvent.itemIndex
            )

        is AnimeListView.UiEvent.NotificationClick ->
            AnnouncedSectionStore.Intent.NotificationClick(
                itemIndex = uiEvent.itemIndex
            )

        AnimeListView.UiEvent.UpdateOngoingsSection,
        AnimeListView.UiEvent.UpdateSearchSection,
        AnimeListView.UiEvent.OngoingsSectionClick,
        AnimeListView.UiEvent.AnnouncedSectionClick,
        AnimeListView.UiEvent.SearchSectionClick,
        AnimeListView.UiEvent.CancelSearchClick,
        is AnimeListView.UiEvent.SearchTextChange -> null

    }
}

internal fun mapUiEventToSearchSectionIntent(uiEvent: AnimeListView.UiEvent)
        : SearchSectionStore.Intent? {
    return when (uiEvent) {
        AnimeListView.UiEvent.UpdateSearchSection ->
            SearchSectionStore.Intent.UpdateSection

        is AnimeListView.UiEvent.SearchTextChange ->
            SearchSectionStore.Intent.SearchTextChange(
                searchText = uiEvent.searchText
            )

        is AnimeListView.UiEvent.EpisodesInfoClick ->
            SearchSectionStore.Intent.EpisodesInfoClick(
                itemIndex = uiEvent.itemIndex
            )

        is AnimeListView.UiEvent.NotificationClick ->
            SearchSectionStore.Intent.NotificationClick(
                itemIndex = uiEvent.itemIndex
            )

        AnimeListView.UiEvent.UpdateOngoingsSection,
        AnimeListView.UiEvent.UpdateAnnouncedSection,
        AnimeListView.UiEvent.UpdateOngoingsSection,
        AnimeListView.UiEvent.UpdateSearchSection,
        AnimeListView.UiEvent.OngoingsSectionClick,
        AnimeListView.UiEvent.AnnouncedSectionClick,
        AnimeListView.UiEvent.SearchSectionClick,
        AnimeListView.UiEvent.CancelSearchClick -> null
    }
}