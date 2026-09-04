package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal fun mapStateToUiModel(state: AnimeFavoritesMainStore.State): UiModel {
    return UiModel(
        listItems = getListItemsUi(state),
        contentType = mapContentTypeDomainToUi(state)
    )
}

private fun getListItemsUi(state: AnimeFavoritesMainStore.State): ImmutableList<ListItemUi> {
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
    }.toPersistentList()
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
    val episodesTotalString = if (episodesTotalNotNull > 0) {
        episodesTotalNotNull.toString()
    } else {
        "?"
    }

    return "$episodesAiredString / $episodesTotalString"
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
        is ContentTypeDomain.LOADING -> ContentTypeUi.LOADING
        ContentTypeDomain.LOADED -> ContentTypeUi.LOADED
        ContentTypeDomain.EMPTY -> ContentTypeUi.EMPTY
    }
}
