package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.database.api.domain.model.AnimeDb
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: DatabaseStore.State
): AnimeFavoritesMainStore.Intent {
    return AnimeFavoritesMainStore.Intent.UpdateListItems(
        listItems = mapDbItemsToDomain(state.animeDatabaseItems)
    )
}

private fun mapDbItemsToDomain(animeDatabaseItems: List<AnimeDb>): List<ListItemDomain> {
    return animeDatabaseItems.map { dbItem: AnimeDb ->
        dbItem.toDomain()
    }
}