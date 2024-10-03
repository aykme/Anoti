package com.alekseivinogradov.anime_list.api.domain.mapper.store

import com.alekseivinogradov.anime_list.api.data.mapper.toDb
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapMainStoreLabelToDatabaseStoreIntent(
    label: AnimeListMainStore.Label
): DatabaseStore.Intent? {
    return when (label) {
        is AnimeListMainStore.Label.EnableNotification -> {
            DatabaseStore.Intent.InsertAnimeDatabaseItem(label.listItem.toDb())
        }

        is AnimeListMainStore.Label.DisableNotification -> {
            DatabaseStore.Intent.DeleteAnimeDatabaseItem(label.id)
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
        is AnimeListMainStore.Label.ChangeSearchText -> null
    }
}
