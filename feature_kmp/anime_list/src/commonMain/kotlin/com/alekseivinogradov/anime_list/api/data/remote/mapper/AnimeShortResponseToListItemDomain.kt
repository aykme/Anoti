package com.alekseivinogradov.anime_list.api.data.remote.mapper

import com.alekseivinogradov.anime_list.api.domain.model.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_network_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeShortResponse
import com.alekseivinogradov.anime_network_base.api.data.remote.response.ImageResponse
import com.alekseivinogradov.network.api.domain.SHIKIMORI_BASE_URL

internal fun AnimeShortResponse.toListItemDomain() = ListItemDomain(
    id = this.id,
    name = this.englishName ?: "",
    imageUrl = mapImageUrlDataToDomain(this.imageResponse),
    episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = getExtraEpisodesInfo(
        releaseStatus = this.releaseStatus,
        nextEpisodeAt = null,
        airedOn = this.airedOn,
        releasedOn = this.releasedOn
    ),
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
)


internal fun AnimeDetailsResponse.toListItemDomain() = ListItemDomain(
    id = this.id,
    name = this.englishName ?: "",
    imageUrl = mapImageUrlDataToDomain(this.imageResponse),
    episodesInfoType = EpisodesInfoTypeDomain.AVAILABLE,
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    extraEpisodesInfo = getExtraEpisodesInfo(
        releaseStatus = this.releaseStatus,
        nextEpisodeAt = this.nextEpisodeAt,
        airedOn = this.airedOn,
        releasedOn = this.releasedOn
    ),
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
)

private fun mapImageUrlDataToDomain(imageResponse: ImageResponse?): String? {
    val additionalImageUrl =
        imageResponse?.originalSizeUrl ?: imageResponse?.previewSizeUrl
    val fullImageUrl = additionalImageUrl?.let {
        SHIKIMORI_BASE_URL + additionalImageUrl
    }
    return fullImageUrl
}

private fun getExtraEpisodesInfo(
    releaseStatus: String?,
    nextEpisodeAt: String?,
    airedOn: String?,
    releasedOn: String?
): String? {
    return when (releaseStatus) {
        ReleaseStatusData.ONGOING.value -> nextEpisodeAt
        ReleaseStatusData.ANNOUNCED.value -> airedOn
        ReleaseStatusData.RELEASED.value -> releasedOn
        else -> null
    }
}

private fun mapReleaseStatusDataToDomain(releaseStatus: String?): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusData.ONGOING.value -> ReleaseStatusDomain.ONGOING
        ReleaseStatusData.ANNOUNCED.value -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusData.RELEASED.value -> ReleaseStatusDomain.RELEASED
        else -> ReleaseStatusDomain.UNKNOWN
    }
}