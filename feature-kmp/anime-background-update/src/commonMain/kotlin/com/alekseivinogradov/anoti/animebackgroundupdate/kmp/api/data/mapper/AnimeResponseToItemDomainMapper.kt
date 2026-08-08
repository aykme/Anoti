package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapImageUrlDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse

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
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
)
