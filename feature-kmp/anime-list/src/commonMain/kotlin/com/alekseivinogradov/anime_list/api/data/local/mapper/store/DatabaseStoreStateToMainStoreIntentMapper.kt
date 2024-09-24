package com.alekseivinogradov.anime_list.api.data.local.mapper.store

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: DatabaseStore.State
): AnimeListMainStore.Intent {
    return AnimeListMainStore.Intent.UpdateEnabledNotificationIds(
        enabledNotificationIds = getEnabledNotificationIds(state)
    )
}

private fun getEnabledNotificationIds(state: DatabaseStore.State): Set<AnimeId> {
    return state.animeDatabaseItems.map {
        it.id
    }.toSet()
}
