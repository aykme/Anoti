package com.alekseivinogradov.anime_list.api.data.local.mapper.store

import com.alekseivinogradov.anime_list.api.data.local.mapper.toDb
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapOngoingSectionLabelToDatabaseIntent(label: OngoingSectionStore.Label)
        : DatabaseStore.Intent {
    return when (label) {
        is OngoingSectionStore.Label.EnableNotification -> {
            DatabaseStore.Intent.InsertAnimeDatabaseItem(label.listItem.toDb())
        }

        is OngoingSectionStore.Label.DisableNotification -> {
            DatabaseStore.Intent.DeleteAnimeDatabaseItem(label.id)
        }
    }
}

internal fun mapAnnouncedSectionLabelToDatabaseIntent(label: AnnouncedSectionStore.Label)
        : DatabaseStore.Intent {
    return when (label) {
        is AnnouncedSectionStore.Label.EnableNotification -> {
            DatabaseStore.Intent.InsertAnimeDatabaseItem(label.listItem.toDb())
        }

        is AnnouncedSectionStore.Label.DisableNotification -> {
            DatabaseStore.Intent.DeleteAnimeDatabaseItem(label.id)
        }
    }
}

internal fun mapSearchSectionLabelToDatabaseIntent(label: SearchSectionStore.Label)
        : DatabaseStore.Intent {
    return when (label) {
        is SearchSectionStore.Label.EnableNotification -> {
            DatabaseStore.Intent.InsertAnimeDatabaseItem(label.listItem.toDb())
        }

        is SearchSectionStore.Label.DisableNotification -> {
            DatabaseStore.Intent.DeleteAnimeDatabaseItem(label.id)
        }
    }
}