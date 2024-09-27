package com.alekseivinogradov.anime_list.api.presentation.mapper.model

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anime_list.api.presentation.model.UiModel
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ReleaseStatusUi

internal fun mapStateToUiModel(
    state: AnimeListMainStore.State
): UiModel {
    return UiModel(
        selectedSection = mapSelectedSectionDomainToUi(state.selectedSection),
        search = mapSearchDomainToUi(state.search),
        contentType = getContentTypeUi(state),
        listItems = getListItemsUi(state),
    )
}

private fun mapSelectedSectionDomainToUi(selectedSection: SectionHatDomain): SectionHatUi {
    return when (selectedSection) {
        SectionHatDomain.ONGOINGS -> SectionHatUi.ONGOINGS
        SectionHatDomain.ANNOUNCED -> SectionHatUi.ANNOUNCED
        SectionHatDomain.SEARCH -> SectionHatUi.SEARCH
    }
}

private fun mapSearchDomainToUi(search: SearchDomain): SearchUi {
    return when (search.type) {
        SearchDomain.Type.HIDEN -> SearchUi.HIDEN
        SearchDomain.Type.SHOWN -> SearchUi.SHOWN
    }
}

private fun getContentTypeUi(state: AnimeListMainStore.State): ContentTypeUi {
    val contentType = when (state.selectedSection) {
        SectionHatDomain.ONGOINGS -> state.ongoingContent.contentType
        SectionHatDomain.ANNOUNCED -> state.announcedContent.contentType
        SectionHatDomain.SEARCH -> state.searchContent.contentType
    }
    return when (contentType) {
        ContentTypeDomain.LOADED -> ContentTypeUi.LOADED
        ContentTypeDomain.LOADING -> ContentTypeUi.LOADING
        ContentTypeDomain.ERROR -> ContentTypeUi.ERROR
    }
}

private fun getListItemsUi(
    state: AnimeListMainStore.State
): List<ListItemUi> {
    val listItemsContent = when (state.selectedSection) {
        SectionHatDomain.ONGOINGS -> state.ongoingContent
        SectionHatDomain.ANNOUNCED -> state.announcedContent
        SectionHatDomain.SEARCH -> state.searchContent
    }

    return listItemsContent.listItems.map { listItem: ListItemDomain ->
        getListItemUi(
            listItem = listItem,
            enabledNotificationIds = state.enabledNotificationIds,
            enabledExtraEpisodesInfoIds = listItemsContent.enabledExtraEpisodesInfoIds
        )
    }
}

private fun getListItemUi(
    listItem: ListItemDomain,
    enabledNotificationIds: Set<AnimeId>,
    enabledExtraEpisodesInfoIds: Set<AnimeId>
): ListItemUi {
    return ListItemUi(
        id = listItem.id,
        imageUrl = listItem.imageUrl,
        name = listItem.name,
        episodesInfoType = getEpisodesInfoTypeUi(
            id = listItem.id,
            enabledExtraEpisodesInfoIds = enabledExtraEpisodesInfoIds
        ),
        availableEpisodesInfo = getAvailableEpisodesInfo(listItem),
        extraEpisodesInfo = getExtraEpisodesInfo(listItem),
        score = listItem.score?.toString().orEmpty(),
        releaseStatus = mapReleaseStatusDomainToUi(listItem.releaseStatus),
        notification = mapNotificationDomainToUi(
            listItemId = listItem.id,
            enabledNotificationIds = enabledNotificationIds
        )
    )
}

private fun getEpisodesInfoTypeUi(
    id: AnimeId,
    enabledExtraEpisodesInfoIds: Set<AnimeId>,
): EpisodesInfoTypeUi {
    return if (enabledExtraEpisodesInfoIds.contains(id)) {
        EpisodesInfoTypeUi.EXTRA
    } else {
        EpisodesInfoTypeUi.AVAILABLE
    }
}

private fun getAvailableEpisodesInfo(listItem: ListItemDomain): String {
    val isReleased = listItem.releaseStatus == ReleaseStatusDomain.RELEASED

    val episodesTotal = listItem.episodesTotal ?: 0
    val episodesAiredString = if (isReleased.not()) {
        listItem.episodesAired ?: 0
    } else {
        episodesTotal
    }

    val episotesTotalString = if (episodesTotal > 0) {
        episodesTotal.toString()
    } else "?"

    return "$episodesAiredString / $episotesTotalString"
}

private fun getExtraEpisodesInfo(listItem: ListItemDomain): String? {
    return when (listItem.releaseStatus) {
        ReleaseStatusDomain.ONGOING -> listItem.nextEpisodeAt
        ReleaseStatusDomain.ANNOUNCED -> listItem.airedOn
        ReleaseStatusDomain.RELEASED -> listItem.releasedOn
        ReleaseStatusDomain.UNKNOWN -> null
    }
}

private fun mapReleaseStatusDomainToUi(releaseStatus: ReleaseStatusDomain): ReleaseStatusUi {
    return when (releaseStatus) {
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusUi.UNKNOWN
        ReleaseStatusDomain.ONGOING -> ReleaseStatusUi.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusUi.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusUi.RELEASED
    }
}

private fun mapNotificationDomainToUi(
    listItemId: Int?,
    enabledNotificationIds: Set<Int>
): NotificationUi {
    return if (listItemId != null && enabledNotificationIds.contains(listItemId)) {
        NotificationUi.ENABLED
    } else {
        NotificationUi.DISABLED
    }
}
