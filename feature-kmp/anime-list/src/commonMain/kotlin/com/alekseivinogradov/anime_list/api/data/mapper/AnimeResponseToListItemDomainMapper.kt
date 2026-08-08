package com.alekseivinogradov.anime_list.api.data.mapper

import com.alekseivinogradov.anime_base.api.data.mapper.mapImageUrlDataToDomain
import com.alekseivinogradov.anime_base.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anime_base.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain

internal fun AnimeShortResponse.toListItemDomain() = ListItemDomain(
    /**
     * id - Anime id.
     * id == -1 is fallback. Need to filter null values before using this method
     */
    id = this.id ?: -1,
    name = this.englishName ?: "",
    imageUrl = mapImageUrlDataToDomain(this.imageResponse),
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = null,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
)

internal fun AnimeDetailsResponse.toListItemDomain() = ListItemDomain(
    /**
     * id - Anime id.
     * id == -1 is fallback. Need to filter null values before using this method
     */
    id = this.id ?: -1,
    name = this.englishName ?: "",
    imageUrl = mapImageUrlDataToDomain(this.imageResponse),
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
)
