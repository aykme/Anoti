package com.alekseivinogradov.anime_list.api.domain.store.mapper

import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore

internal fun mapMainStoreLabelToOngoingStoreIntent(
    label: AnimeListMainStore.Label
): OngoingSectionStore.Intent? {
    return when (label) {
        AnimeListMainStore.Label.OpenOngoingSection -> {
            OngoingSectionStore.Intent.OpenSection
        }

        AnimeListMainStore.Label.UpdateOngoingSection -> {
            OngoingSectionStore.Intent.UpdateSection
        }

        is AnimeListMainStore.Label.OngoingEpisodeInfoClick -> {
            OngoingSectionStore.Intent.EpisodesInfoClick(label.itemIndex)
        }

        AnimeListMainStore.Label.OpenAnnouncedSection,
        AnimeListMainStore.Label.UpdateAnnouncedSection,
        AnimeListMainStore.Label.OpenSearchSection,
        AnimeListMainStore.Label.UpdateSearchSection,
        is AnimeListMainStore.Label.ChangeSearchText,
        is AnimeListMainStore.Label.AnnouncedEpisodeInfoClick,
        is AnimeListMainStore.Label.SearchEpisodeInfoClick,
        is AnimeListMainStore.Label.DisableNotification,
        is AnimeListMainStore.Label.EnableNotification -> null
    }
}

internal fun mapMainStoreLabelToAnnouncedStoreIntent(
    label: AnimeListMainStore.Label
): AnnouncedSectionStore.Intent? {
    return when (label) {
        AnimeListMainStore.Label.OpenAnnouncedSection -> {
            AnnouncedSectionStore.Intent.OpenSection
        }

        AnimeListMainStore.Label.UpdateAnnouncedSection -> {
            AnnouncedSectionStore.Intent.UpdateSection
        }

        is AnimeListMainStore.Label.AnnouncedEpisodeInfoClick -> {
            AnnouncedSectionStore.Intent.EpisodesInfoClick(label.itemIndex)
        }

        AnimeListMainStore.Label.OpenOngoingSection,
        AnimeListMainStore.Label.UpdateOngoingSection,
        AnimeListMainStore.Label.OpenSearchSection,
        AnimeListMainStore.Label.UpdateSearchSection,
        is AnimeListMainStore.Label.ChangeSearchText,
        is AnimeListMainStore.Label.OngoingEpisodeInfoClick,
        is AnimeListMainStore.Label.SearchEpisodeInfoClick,
        is AnimeListMainStore.Label.DisableNotification,
        is AnimeListMainStore.Label.EnableNotification -> null
    }
}

internal fun mapMainStoreLabelToSearchStoreIntent(
    label: AnimeListMainStore.Label
): SearchSectionStore.Intent? {
    return when (label) {
        AnimeListMainStore.Label.OpenSearchSection -> {
            SearchSectionStore.Intent.OpenSection
        }

        AnimeListMainStore.Label.UpdateSearchSection -> {
            SearchSectionStore.Intent.UpdateSection
        }

        is AnimeListMainStore.Label.SearchEpisodeInfoClick -> {
            SearchSectionStore.Intent.EpisodesInfoClick(label.itemIndex)
        }

        is AnimeListMainStore.Label.ChangeSearchText -> {
            SearchSectionStore.Intent.ChangeSearchText(label.searchText)
        }

        AnimeListMainStore.Label.OpenOngoingSection,
        AnimeListMainStore.Label.UpdateOngoingSection,
        AnimeListMainStore.Label.OpenAnnouncedSection,
        AnimeListMainStore.Label.UpdateAnnouncedSection,
        is AnimeListMainStore.Label.AnnouncedEpisodeInfoClick,
        is AnimeListMainStore.Label.OngoingEpisodeInfoClick,
        is AnimeListMainStore.Label.DisableNotification,
        is AnimeListMainStore.Label.EnableNotification -> null
    }
}
