package com.alekseivinogradov.anoti.animefavorites.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapImageUrlDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain

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
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus),
    episodesViewed = 0,
    isNewEpisode = false
)
