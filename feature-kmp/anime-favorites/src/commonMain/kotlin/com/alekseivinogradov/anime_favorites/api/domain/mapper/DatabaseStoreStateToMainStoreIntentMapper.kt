package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.store.DatabaseStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: DatabaseStore.State
): AnimeFavoritesMainStore.Intent {
    return AnimeFavoritesMainStore.Intent.UpdateListItems(
        listItems = mapDbItemsToDomain(state.animeDatabaseItems)
    )
}

private fun mapDbItemsToDomain(animeDatabaseItems: List<AnimeDbDomain>): List<ListItemDomain> {
    return animeDatabaseItems.map { dbItem: AnimeDbDomain ->
        dbItem.toDomain()
    }
}
