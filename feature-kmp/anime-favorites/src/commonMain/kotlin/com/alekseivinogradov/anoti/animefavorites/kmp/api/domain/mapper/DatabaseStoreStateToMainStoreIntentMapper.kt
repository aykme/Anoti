package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore

internal fun mapDatabaseStoreStateToMainStoreIntent(
    state: AnimeDatabaseStore.State
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
