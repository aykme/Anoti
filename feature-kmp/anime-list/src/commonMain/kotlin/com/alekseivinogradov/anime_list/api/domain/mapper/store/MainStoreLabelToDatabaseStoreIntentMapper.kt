package com.alekseivinogradov.anime_list.api.domain.mapper.store

import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_list.api.domain.mapper.toDb
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore

internal fun mapMainStoreLabelToDatabaseStoreIntent(
    label: AnimeListMainStore.Label
): AnimeDatabaseStore.Intent? {
    return when (label) {
        is AnimeListMainStore.Label.EnableNotificationClick -> {
            AnimeDatabaseStore.Intent.InsertAnimeDatabaseItem(label.listItem.toDb())
        }

        is AnimeListMainStore.Label.DisableNotificationClick -> {
            AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(label.id)
        }

        AnimeListMainStore.Label.OpenAnnouncedSection,
        AnimeListMainStore.Label.OpenOngoingSection,
        AnimeListMainStore.Label.OpenSearchSection,
        AnimeListMainStore.Label.UpdateAnnouncedSection,
        AnimeListMainStore.Label.UpdateOngoingSection,
        AnimeListMainStore.Label.UpdateSearchSection,
        is AnimeListMainStore.Label.AnnouncedEpisodeInfoClick,
        is AnimeListMainStore.Label.OngoingEpisodeInfoClick,
        is AnimeListMainStore.Label.SearchEpisodeInfoClick,
        is AnimeListMainStore.Label.ChangeSearchText,
        AnimeListMainStore.Label.LoadNextPageOngoingSection,
        AnimeListMainStore.Label.LoadNextPageAnnouncedSection,
        AnimeListMainStore.Label.LoadNextPageSearchSection -> null
    }
}
