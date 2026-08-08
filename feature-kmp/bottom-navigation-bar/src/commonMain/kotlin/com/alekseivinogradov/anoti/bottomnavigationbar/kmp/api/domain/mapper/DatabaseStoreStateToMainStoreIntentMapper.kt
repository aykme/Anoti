package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: AnimeDatabaseStore.State
): BottomNavigationBarStore.Intent {
    return BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(
        favoritesBadgeNumber = getFavoritesBadgeNumber(state)
    )
}

private fun getFavoritesBadgeNumber(state: AnimeDatabaseStore.State): Int {
    val newEpisodesOnlyList = state.animeDatabaseItems.filter {
        it.isNewEpisode
    }
    return newEpisodesOnlyList.count()
}
