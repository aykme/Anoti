package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore

internal fun mapDatabaseStoreLabelToMainStoreIntent(
    label: AnimeDatabaseStore.Label
): AnimeFavoritesMainStore.Intent {
    return when (label) {
        AnimeDatabaseStore.Label.ResetAllItemsNewEpisodeStatusWasFinished -> {
            AnimeFavoritesMainStore.Intent.UpdateAllItemsInBackground
        }
    }
}
