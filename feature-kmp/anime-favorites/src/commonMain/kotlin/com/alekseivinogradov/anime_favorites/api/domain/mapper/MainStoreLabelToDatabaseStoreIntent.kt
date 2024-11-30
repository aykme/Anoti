package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore

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
