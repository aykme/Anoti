package com.alekseivinogradov.anime_list.api.data.remote.mapper

import com.alekseivinogradov.anime_list.api.domain.model.EpisodesInfoTypeDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anime_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_base.api.data.remote.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.remote.response.AnimeShortResponse
import com.alekseivinogradov.anime_base.api.data.remote.response.ImageResponse
import com.alekseivinogradov.network.api.domain.SHIKIMORI_BASE_URL

/**
 * @param id - Anime id.
 * id == -1 is fallback. Need to filter null values before using this method
 */
internal fun AnimeShortResponse.toListItemDomain() = ListItemDomain(
    id = this.id ?: -1,
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


/**
 * @param id - Anime id.
 * id == -1 is fallback. Need to filter null values before using this method
 */
internal fun AnimeDetailsResponse.toListItemDomain() = ListItemDomain(
    id = this.id ?: -1,
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
