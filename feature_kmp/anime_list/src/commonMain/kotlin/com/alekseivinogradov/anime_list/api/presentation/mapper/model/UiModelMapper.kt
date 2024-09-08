package com.alekseivinogradov.anime_list.api.presentation.mapper.model

import com.alekseivinogradov.anime_list.api.domain.model.section.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SectionDomain
import com.alekseivinogradov.anime_list.api.domain.store.announced_section.AnnouncedSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.ongoing_section.OngoingSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.search_section.SearchSectionStore
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore
import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.api.presentation.model.UiModel
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ReleaseStatusUi

internal fun mapStateToUiModel(
    upperMenuState: UpperMenuStore.State,
    ongoingSectionState: OngoingSectionStore.State,
    announcedSectionState: AnnouncedSectionStore.State,
    searchSectionState: SearchSectionStore.State
): UiModel {
    return UiModel(
        selectedSection = mapSelectedSectionDomainToUi(upperMenuState.selectedSection),
        search = mapSearchDomainToUi(upperMenuState.search),
        contentType = getContentType(
            selectedSection = upperMenuState.selectedSection,
            ongoingSectionContentType = ongoingSectionState.contentType,
            announcedSectionContentType = announcedSectionState.contentType,
            searchSectionContentType = searchSectionState.contentType,
        ),
        ongoingListItems = mapListItemsDomainToUi(
            listItems = ongoingSectionState.listItems,
            enabledNotificationIds = ongoingSectionState.enabledNotificationIds
        ),
        announcedListItems = mapListItemsDomainToUi(
            listItems = announcedSectionState.listItems,
            enabledNotificationIds = setOf()
        ),
        searchListItems = mapListItemsDomainToUi(
            listItems = searchSectionState.listItems,
            enabledNotificationIds = setOf()
        )
    )
}

private fun mapSelectedSectionDomainToUi(selectedSection: SectionDomain): SectionUi {
    return when (selectedSection) {
        SectionDomain.ONGOINGS -> SectionUi.ONGOINGS
        SectionDomain.ANNOUNCED -> SectionUi.ANNOUNCED
        SectionDomain.SEARCH -> SectionUi.SEARCH
    }
}

private fun mapSearchDomainToUi(search: SearchDomain): SearchUi {
    return when (search.type) {
        SearchDomain.Type.HIDEN -> SearchUi.HIDEN
        SearchDomain.Type.SHOWN -> SearchUi.SHOWN
    }
}

private fun getContentType(
    selectedSection: SectionDomain,
    ongoingSectionContentType: ContentTypeDomain,
    announcedSectionContentType: ContentTypeDomain,
    searchSectionContentType: ContentTypeDomain
): ContentTypeUi {
    return when (selectedSection) {
        SectionDomain.ONGOINGS -> mapContentTypeDomainToUi(ongoingSectionContentType)
        SectionDomain.ANNOUNCED -> mapContentTypeDomainToUi(announcedSectionContentType)
        SectionDomain.SEARCH -> mapContentTypeDomainToUi(searchSectionContentType)
    }
}

private fun mapContentTypeDomainToUi(contentType: ContentTypeDomain): ContentTypeUi {
    return when (contentType) {
        ContentTypeDomain.LOADED -> ContentTypeUi.LOADED
        ContentTypeDomain.LOADING -> ContentTypeUi.LOADING
        ContentTypeDomain.NO_DATA -> ContentTypeUi.NO_DATA
    }
}

private fun mapListItemsDomainToUi(
    listItems: List<ListItemDomain>,
    enabledNotificationIds: Set<Int>
): List<ListItemUi> {
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