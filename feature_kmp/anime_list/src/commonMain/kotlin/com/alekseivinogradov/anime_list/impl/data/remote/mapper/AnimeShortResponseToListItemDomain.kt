package com.alekseivinogradov.anime_list.impl.data.remote.mapper

import com.alekseivinogradov.anime_list.api.domain.model.section_content.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.NotificationDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ReleaseStatusDomain
import com.alekseivinogradov.anime_network_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeShortResponse

internal fun AnimeShortResponse.toOngoingListItem() = this.toListItemDomain(
    extraEpisodesInfo = null
)

internal fun AnimeShortResponse.toAnnouncedListItem() = this.toListItemDomain(
    extraEpisodesInfo = this.airedOn
)

internal fun AnimeShortResponse.toSearchListItem() = this.toListItemDomain(
    extraEpisodesInfo = this.releasedOn
)

private fun AnimeShortResponse.toListItemDomain(
    extraEpisodesInfo: String?
) = ListItemDomain(
    id = this.id,
    name = this.englishName ?: "",
    imageUrl = this.imageResponse?.originalSizeUrl ?: this.imageResponse?.previewSizeUrl,
    episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = extraEpisodesInfo,
    score = this.score,
    releaseStatus = getReleaseStatus(this.releaseStatus),
    notification = NotificationDomain.DISABLED
)

private fun getReleaseStatus(releaseStatus: String?): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusData.ONGOING.value -> ReleaseStatusDomain.ONGOING
        ReleaseStatusData.ANNOUNCED.value -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusData.RELEASED.value -> ReleaseStatusDomain.RELEASED
        else -> ReleaseStatusDomain.UNKNOWN
    }
}