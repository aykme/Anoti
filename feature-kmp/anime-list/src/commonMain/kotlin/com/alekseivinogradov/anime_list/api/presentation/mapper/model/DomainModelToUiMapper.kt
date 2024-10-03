package com.alekseivinogradov.anime_list.api.presentation.mapper.model

import app.cash.paging.PagingData
import app.cash.paging.map
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_list.api.domain.model.ContentTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_list.api.domain.model.SearchDomain
import com.alekseivinogradov.anime_list.api.domain.model.SectionHatDomain
import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.ListContentUi
import com.alekseivinogradov.anime_list.api.presentation.model.SearchUi
import com.alekseivinogradov.anime_list.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anime_list.api.presentation.model.UiModel
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.EpisodesInfoTypeUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.ListItemUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.NotificationUi
import com.alekseivinogradov.anime_list.api.presentation.model.item_content.ReleaseStatusUi

internal fun mapStateToUiModel(
    state: AnimeListMainStore.State
): UiModel {
    return UiModel(
        selectedSection = mapSelectedSectionDomainToUi(state.selectedSection),
        search = mapSearchDomainToUi(state.search),
        contentType = getContentTypeUi(state),
        listContent = getListContentUi(state)
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

private fun getListContentUi(state: AnimeListMainStore.State): ListContentUi {
    return ListContentUi(
        listItems = getListItemsUi(state),
        isNeedToResetListPositon = state.isNeedToResetListPositon
    )
}

private fun getListItemsUi(
    state: AnimeListMainStore.State
): PagingData<ListItemUi> {
    val listItemsContent = when (state.selectedSection) {
        SectionHatDomain.ONGOINGS -> state.ongoingContent
        SectionHatDomain.ANNOUNCED -> state.announcedContent
        SectionHatDomain.SEARCH -> state.searchContent
    }

    return listItemsContent.listItems.map { listItem: ListItemDomain ->
        getListItemUi(
            listItem = listItem,
            enabledNotificationIds = state.enabledNotificationIds,
            enabledExtraEpisodesInfoIds = listItemsContent.enabledExtraEpisodesInfoIds,
            nextEpisodesInfo = listItemsContent.nextEpisodesInfo
        )
    }
}

private fun getListItemUi(
    listItem: ListItemDomain,
    enabledNotificationIds: Set<AnimeId>,
    enabledExtraEpisodesInfoIds: Set<AnimeId>,
    nextEpisodesInfo: Map<AnimeId, String>
): ListItemUi {
    return ListItemUi(
        id = listItem.id,
        imageUrl = listItem.imageUrl,
        name = listItem.name,
        episodesInfoType = getEpisodesInfoTypeUi(
            id = listItem.id,
            enabledExtraEpisodesInfoIds = enabledExtraEpisodesInfoIds
        ),
        episodesAired = listItem.episodesAired,
        episodesTotal = listItem.episodesTotal,
        nextEpisodeAt = nextEpisodesInfo[listItem.id],
        airedOn = listItem.airedOn,
        releasedOn = listItem.releasedOn,
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
