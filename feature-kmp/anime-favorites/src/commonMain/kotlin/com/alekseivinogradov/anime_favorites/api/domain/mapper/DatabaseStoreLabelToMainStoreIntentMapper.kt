package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapDatabaseStoreLabelToMainStoreIntent(
    label: DatabaseStore.Label
): AnimeFavoritesMainStore.Intent {
    return when (label) {
        DatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished -> {
            AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground
        }
    }
}
