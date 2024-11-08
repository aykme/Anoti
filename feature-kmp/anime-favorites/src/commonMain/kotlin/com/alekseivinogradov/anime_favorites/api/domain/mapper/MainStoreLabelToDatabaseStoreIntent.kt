package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapMainStoreLabelToDatabaseStoreIntent(
    label: AnimeFavoritesMainStore.Label
): DatabaseStore.Intent {
    return when (label) {
        AnimeFavoritesMainStore.Label.UpdateSection -> {
            DatabaseStore.Intent.ResetAllItemsNewEpisodeStatus
        }

        is AnimeFavoritesMainStore.Label.ItemClick -> {
            DatabaseStore.Intent.ChangeItemNewEpisodeStatus(
                id = label.id,
                isNewEpisode = false
            )
        }

        is AnimeFavoritesMainStore.Label.DisableNotificationClick -> {
            DatabaseStore.Intent.DeleteAnimeDatabaseItem(
                id = label.id
            )
        }

        is AnimeFavoritesMainStore.Label.UpdateListItem -> {
            DatabaseStore.Intent.UpdateAnimeDatabaseItem(
                animeDatabaseItem = label.listItem.toDb()
            )
        }
    }
}
