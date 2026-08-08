package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore

internal fun mapMainStoreLabelToDatabaseStoreIntent(
    label: AnimeFavoritesMainStore.Label
): AnimeDatabaseStore.Intent {
    return when (label) {
        AnimeFavoritesMainStore.Label.UpdateSection -> {
            AnimeDatabaseStore.Intent.ResetAllItemsNewEpisodeStatus
        }

        is AnimeFavoritesMainStore.Label.ItemClick -> {
            AnimeDatabaseStore.Intent.ChangeItemNewEpisodeStatus(
                id = label.id,
                isNewEpisode = false
            )
        }

        is AnimeFavoritesMainStore.Label.DisableNotificationClick -> {
            AnimeDatabaseStore.Intent.DeleteAnimeDatabaseItem(
                id = label.id
            )
        }

        is AnimeFavoritesMainStore.Label.UpdateListItem -> {
            AnimeDatabaseStore.Intent.UpdateAnimeDatabaseItem(
                animeDatabaseItem = label.listItem.toDb()
            )
        }
    }
}
