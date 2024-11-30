package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore

internal fun mapDatabaseStoreLabelToMainStoreIntent(
    label: AnimeDatabaseStore.Label
): AnimeFavoritesMainStore.Intent {
    return when (label) {
        AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished -> {
            AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground
        }
    }
}
