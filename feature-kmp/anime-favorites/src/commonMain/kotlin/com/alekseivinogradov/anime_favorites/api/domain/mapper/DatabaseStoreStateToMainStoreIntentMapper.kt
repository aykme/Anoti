package com.alekseivinogradov.anime_favorites.api.domain.mapper

import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.database.api.data.model.AnimeDb
import com.alekseivinogradov.database.api.data.model.ReleaseStatusDb
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
        ListItemDomain(
            id = dbItem.id,
            name = dbItem.name,
            imageUrl = dbItem.imageUrl,
            episodesAired = dbItem.episodesAired,
            episodesTotal = dbItem.episodesTotal,
            nextEpisodeAt = dbItem.nextEpisodeAt,
            airedOn = dbItem.airedOn,
            releasedOn = dbItem.releasedOn,
            score = dbItem.score,
            releaseStatus = mapReleaseStatusDbToDomain(dbItem.releaseStatus),
            episodesViewed = dbItem.episodesViewed,
            isNewEpisode = dbItem.isNewEpisode
        )
    }
}

private fun mapReleaseStatusDbToDomain(releaseStatus: ReleaseStatusDb): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusDb.ONGOING -> ReleaseStatusDomain.ONGOING
        ReleaseStatusDb.ANNOUNCED -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusDb.RELEASED -> ReleaseStatusDomain.RELEASED
        ReleaseStatusDb.UNKNOWN -> ReleaseStatusDomain.UNKNOWN
    }
}
