package com.alekseivinogradov.anime_list.impl.data.remote.mapper

import com.alekseivinogradov.anime_list.api.domain.model.section_content.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.NotificationDomain
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ReleaseStatusDomain
import com.alekseivinogradov.anime_network_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeShortResponse
import com.alekseivinogradov.anime_network_base.api.data.remote.response.ImageResponse
import com.alekseivinogradov.network.api.domain.SHIKIMORI_BASE_URL

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
    imageUrl = mapImageUrlDataToDomain(this.imageResponse),
    episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = extraEpisodesInfo,
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus),
    notification = NotificationDomain.DISABLED
)

private fun mapImageUrlDataToDomain(imageResponse: ImageResponse?): String? {
    val additionalImageUrl =
        imageResponse?.originalSizeUrl ?: imageResponse?.previewSizeUrl
    val fullImageUrl = additionalImageUrl?.let {
        SHIKIMORI_BASE_URL + additionalImageUrl
    }
    return fullImageUrl
}

private fun mapReleaseStatusDataToDomain(releaseStatus: String?): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusData.ONGOING.value -> ReleaseStatusDomain.ONGOING
        ReleaseStatusData.ANNOUNCED.value -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusData.RELEASED.value -> ReleaseStatusDomain.RELEASED
        else -> ReleaseStatusDomain.UNKNOWN
    }
}