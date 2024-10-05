package com.alekseivinogradov.bottom_navigation_bar.api.domain.mapper

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: DatabaseStore.State
): BottomNavigationBarStore.Intent {
    return BottomNavigationBarStore.Intent.UpdateFavoritesBadgeNumber(
        favoritesBadgeNumber = getFavoritesBadgeNumber(state)
    )
}

private fun getFavoritesBadgeNumber(state: DatabaseStore.State): Int {
    val newEpisodesOnlyList = state.animeDatabaseItems.filter {
        it.isNewEpisode == true
    }
    return newEpisodesOnlyList.count()
}
