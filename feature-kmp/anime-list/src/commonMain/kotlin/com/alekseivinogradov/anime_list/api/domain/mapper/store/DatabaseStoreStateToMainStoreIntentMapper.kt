package com.alekseivinogradov.anime_list.api.domain.mapper.store

import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.celebrity.api.domain.AnimeId

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: AnimeDatabaseStore.State
): AnimeListMainStore.Intent {
    return AnimeListMainStore.Intent.UpdateEnabledNotificationIds(
        enabledNotificationIds = getEnabledNotificationIds(state)
    )
}

private fun getEnabledNotificationIds(state: AnimeDatabaseStore.State): Set<AnimeId> {
    return state.animeDatabaseItems.map {
        it.id
    }.toSet()
}
