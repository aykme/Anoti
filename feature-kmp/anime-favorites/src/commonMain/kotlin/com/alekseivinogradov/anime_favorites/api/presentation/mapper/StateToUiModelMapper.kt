package com.alekseivinogradov.anime_favorites.api.presentation.mapper

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.UiModel
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.InfoTypeUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.NotificationUi
import com.alekseivinogradov.anime_favorites.api.presentation.model.item_content.ReleaseStatusUi
import com.alekseivinogradov.celebrity.api.domain.AnimeId

internal fun mapStateToUiModel(state: AnimeFavoritesMainStore.State): UiModel {
    return UiModel(
        listItems = getListItemsUi(state),
        contentType = mapContentTypeDomainToUi(state)
    )
}

private fun getListItemsUi(state: AnimeFavoritesMainStore.State): List<ListItemUi> {
    return state.listItems.map { listItemDomain: ListItemDomain ->
        ListItemUi(
            id = listItemDomain.id,
            imageUrl = listItemDomain.imageUrl,
            score = listItemDomain.score?.toString().orEmpty(),
            infoType = getInfoTypeUi(
                id = listItemDomain.id,
                enabledExtraInfoIds = state.enabledExtraInfoIds
            ),
            name = listItemDomain.name,
            availableEpisodesInfo = getAvailableEpisodesInfo(listItemDomain),
            releaseStatus = mapReleaseStatusDomainToUi(listItemDomain.releaseStatus),
            notification = NotificationUi.ENABLED,
            extraEpisodesInfo = getExtraEpisodesInfo(listItemDomain),
            episodesViewed = listItemDomain.episodesViewed.toString(),
            isNewEpisode = listItemDomain.isNewEpisode
        )
    }
}

private fun getInfoTypeUi(id: AnimeId, enabledExtraInfoIds: Set<AnimeId>): InfoTypeUi {
    return if (enabledExtraInfoIds.contains(id)) {
        InfoTypeUi.EXTRA
    } else {
        InfoTypeUi.MAIN
    }
}

private fun getAvailableEpisodesInfo(listItemDomain: ListItemDomain): String {
    val isReleased = listItemDomain.releaseStatus == ReleaseStatusDomain.RELEASED

    val episodesAiredString = if (isReleased.not()) {
        listItemDomain.episodesAired ?: 0
    } else {
        listItemDomain.episodesTotal ?: listItemDomain.episodesAired ?: 0
    }

    val episodesTotalNotNull = listItemDomain.episodesTotal ?: 0
    val episotesTotalString = if (episodesTotalNotNull > 0) {
        episodesTotalNotNull.toString()
    } else "?"

    return "$episodesAiredString / $episotesTotalString"
}

private fun mapReleaseStatusDomainToUi(releaseStatus: ReleaseStatusDomain): ReleaseStatusUi {
    return when (releaseStatus) {
        ReleaseStatusDomain.ONGOING -> ReleaseStatusUi.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusUi.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusUi.RELEASED
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusUi.UNKNOWN
    }
}

private fun getExtraEpisodesInfo(listItemDomain: ListItemDomain): String? {
    return when (listItemDomain.releaseStatus) {
        ReleaseStatusDomain.ONGOING -> listItemDomain.nextEpisodeAt
        ReleaseStatusDomain.ANNOUNCED -> listItemDomain.airedOn
        ReleaseStatusDomain.RELEASED -> listItemDomain.releasedOn
        ReleaseStatusDomain.UNKNOWN -> null
    }
}

private fun mapContentTypeDomainToUi(state: AnimeFavoritesMainStore.State): ContentTypeUi {
    return when (state.contentType) {
        ContentTypeDomain.LOADING -> ContentTypeUi.LOADING
        ContentTypeDomain.LOADED -> ContentTypeUi.LOADED
        ContentTypeDomain.EMPTY -> ContentTypeUi.EMPTY
    }
}
