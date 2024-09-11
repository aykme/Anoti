package com.alekseivinogradov.anime_list.api.data.local.mapper.store

import com.alekseivinogradov.anime_list.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapDatabaseStateToOngoingSectionIntent(state: DatabaseStore.State)
        : OngoingSectionStore.Intent {
    return OngoingSectionStore.Intent.UpdateEnabledNotificationIds(
        enabledNotificationIds = getEnabledNotificationIds(state)
    )
}

internal fun mapDatabaseStateToAnnouncedSectionIntent(state: DatabaseStore.State)
        : AnnouncedSectionStore.Intent {
    return AnnouncedSectionStore.Intent.UpdateEnabledNotificationIds(
        enabledNotificationIds = getEnabledNotificationIds(state)
    )
}

internal fun mapDatabaseStateToSearchSectionIntent(state: DatabaseStore.State)
        : SearchSectionStore.Intent {
    return SearchSectionStore.Intent.UpdateEnabledNotificationIds(
        enabledNotificationIds = getEnabledNotificationIds(state)
    )
}

private fun getEnabledNotificationIds(state: DatabaseStore.State): Set<AnimeId> {
    return state.animeDatabaseItems.map {
        it.id
    }.toSet()
}