package com.alekseivinogradov.anime_background_update.api.data.mapper

import com.alekseivinogradov.anime_background_update.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_base.api.data.mapper.mapImageUrlDataToDomain
import com.alekseivinogradov.anime_base.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortResponse

/**
 * @param id - Anime id.
 * id == -1 is fallback. Need to filter null values before using this method
 */
internal fun AnimeShortResponse.toListItemDomain() = ListItemDomain(
    id = this.id ?: -1,
    name = this.englishName ?: "",
    imageUrl = mapImageUrlDataToDomain(this.imageResponse),
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
)
