package com.alekseivinogradov.anime_list.api.presentation.mapper.model

import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.EpisodesInfoTypeDomain
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
        ContentTypeDomain.NO_DATA -> ContentTypeUi.NO_DATA
    }
}

private fun getListItemsUi(
    state: AnimeListMainStore.State
): List<ListItemUi> {
    val listItems = when (state.selectedSection) {
        SectionHatDomain.ONGOINGS -> state.ongoingContent.listItems
        SectionHatDomain.ANNOUNCED -> state.announcedContent.listItems
        SectionHatDomain.SEARCH -> state.searchContent.listItems
    }
    val enabledNotificationIds = state.enabledNotificationIds

    return listItems.mapIndexed { itemIndex: Int, listItem: ListItemDomain ->
        mapListItemDomainToUi(
            itemIndex = itemIndex,
            listItem = listItem,
            enabledNotificationIds = enabledNotificationIds
        )
    }
}

private fun mapListItemDomainToUi(
    itemIndex: Int,
    listItem: ListItemDomain,
    enabledNotificationIds: Set<Int>
): ListItemUi {
    return ListItemUi(
        itemIndex = itemIndex,
        imageUrl = listItem.imageUrl,
        name = listItem.name,
        episodesInfoType = mapEpisodeInfoTypeDomainToUi(listItem.episodesInfoType),
        availableEpisodesInfo = getAvailableEpisodesInfo(
            episodesAired = listItem.episodesAired ?: 0,
            episodesTotal = listItem.episodesTotal ?: 0,
            releaseStatus = listItem.releaseStatus
        ),
        extraEpisodesInfo = listItem.extraEpisodesInfo,
        score = listItem.score?.toString().orEmpty(),
        releaseStatus = mapReleaseStatusDomainToUi(listItem.releaseStatus),
        notification = mapNotificationDomainToUi(
            listItemId = listItem.id,
            enabledNotificationIds = enabledNotificationIds
        )
    )
}

private fun mapEpisodeInfoTypeDomainToUi(
    episodesInfoType: EpisodesInfoTypeDomain
): EpisodesInfoTypeUi {
    return when (episodesInfoType) {
        EpisodesInfoTypeDomain.AVAILABLE -> EpisodesInfoTypeUi.AVAILABLE
        EpisodesInfoTypeDomain.EXTRA -> EpisodesInfoTypeUi.EXTRA
    }
}

private fun getAvailableEpisodesInfo(
    episodesAired: Int,
    episodesTotal: Int,
    releaseStatus: ReleaseStatusDomain
): String {
    val isReleased = releaseStatus == ReleaseStatusDomain.RELEASED

    val episodesAiredString = if (isReleased.not()) {
        episodesAired
    } else {
        episodesTotal
    }

    val episotesTotalString = if (episodesTotal > 0) {
        episodesTotal.toString()
    } else "?"

    return "$episodesAiredString / $episotesTotalString"
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
