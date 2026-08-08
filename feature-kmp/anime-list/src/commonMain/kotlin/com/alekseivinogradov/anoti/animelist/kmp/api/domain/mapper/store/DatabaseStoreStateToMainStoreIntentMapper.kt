package com.alekseivinogradov.anoti.animelist.kmp.api.domain.mapper.store

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

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
