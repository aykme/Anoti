package com.alekseivinogradov.anime_list.impl.presentation.mapper.model

import com.alekseivinogradov.anime_list.api.domain.model.section_content.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.NotificationDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.upper_menu.SectionDomain
import com.alekseivinogradov.anime_list.api.domain.store.section_content.SectionContentStore
import com.alekseivinogradov.anime_list.api.domain.store.upper_menu.UpperMenuStore
import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.list_content.ReleaseStatusUi

internal fun mapStateToUiModel(
    upperMenuState: UpperMenuStore.State,
    ongoingSectionContentState: SectionContentStore.State
): AnimeListView.UiModel {
    return AnimeListView.UiModel(
        selectedSection = mapSelectedSectionDomainToUi(upperMenuState.selectedSection),
        search = mapSearchDomainToUi(upperMenuState.search),
        contentType = getContentType(
            selectedSection = upperMenuState.selectedSection,
            ongoingSectionContentType = ongoingSectionContentState.contentType
        ),
        ongoingListItems = mapListItemsDomainToUi(ongoingSectionContentState.listItems)
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
    ongoingSectionContentType: ContentTypeDomain
): ContentTypeUi {
    return when (selectedSection) {
        SectionDomain.ONGOINGS -> mapContentTypeDomainToUi(ongoingSectionContentType)
        SectionDomain.ANNOUNCED -> ContentTypeUi.LOADING
        SectionDomain.SEARCH -> ContentTypeUi.LOADING
    }
}

private fun mapContentTypeDomainToUi(contentType: ContentTypeDomain): ContentTypeUi {
    return when (contentType) {
        ContentTypeDomain.LOADED -> ContentTypeUi.LOADED
        ContentTypeDomain.LOADING -> ContentTypeUi.LOADING
        ContentTypeDomain.NO_DATA -> ContentTypeUi.NO_DATA
    }
}

private fun mapListItemsDomainToUi(listItems: List<ListItemDomain>): List<ListItemUi> {
    return listItems.mapIndexed { itemIndex: Int, listItem: ListItemDomain ->
        mapListItemDomainToUi(
            itemIndex = itemIndex,
            listItem = listItem
        )
    }
}

private fun mapListItemDomainToUi(itemIndex: Int, listItem: ListItemDomain): ListItemUi {
    return ListItemUi(
        itemIndex = itemIndex,
        imageUrl = listItem.imageUrl,
        name = listItem.name,
        episodesInfoType = EpisodesInfoTypeUi.AVAILABLE,
        availableEpisodesInfo = "1 / 10",
        extraEpisodesInfo = "",
        score = listItem.score?.toString().orEmpty(),
        releaseStatus = mapReleaseStatusDomainToUi(listItem.releaseStatus),
        notification = mapNotificationDomainToUi(listItem.notification)
    )
}

private fun mapReleaseStatusDomainToUi(releaseStatus: ReleaseStatusDomain): ReleaseStatusUi {
    return when (releaseStatus) {
        ReleaseStatusDomain.UNKNOWN -> ReleaseStatusUi.UNKNOWN
        ReleaseStatusDomain.ONGOING -> ReleaseStatusUi.ONGOING
        ReleaseStatusDomain.ANNOUNCED -> ReleaseStatusUi.ANNOUNCED
        ReleaseStatusDomain.RELEASED -> ReleaseStatusUi.RELEASED
    }
}

private fun mapNotificationDomainToUi(notification: NotificationDomain): NotificationUi {
    return when (notification) {
        NotificationDomain.ENABLED -> NotificationUi.ENABLED
        NotificationDomain.DISABLED -> NotificationUi.DISABLED
    }
}