package com.alekseivinogradov.bottom_navigation_bar.api.domain.mapper

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: AnimeDatabaseStore.State
): BottomNavigationBarStore.Intent {
    return BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(
        favoritesBadgeNumber = getFavoritesBadgeNumber(state)
    )
}

private fun getFavoritesBadgeNumber(state: AnimeDatabaseStore.State): Int {
    val newEpisodesOnlyList = state.animeDatabaseItems.filter {
        it.isNewEpisode == true
    }
    return newEpisodesOnlyList.count()
}
