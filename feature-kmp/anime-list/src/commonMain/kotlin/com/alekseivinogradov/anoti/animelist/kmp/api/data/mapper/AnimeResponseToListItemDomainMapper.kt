package com.alekseivinogradov.anoti.animelist.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.animeId
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapPosterUrlDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain

/**
 * One anime of a list page, or null when it carries no usable id: the list keys its rows by id.
 */
internal fun AnimeResponse.toListItemDomain(): ListItemDomain? {
    val animeId = animeId() ?: return null

    return ListItemDomain(
        id = animeId,
        name = this.englishName ?: "",
        imageUrl = mapPosterUrlDataToDomain(this.poster),
        episodesAired = this.episodesAired,
        episodesTotal = this.episodesTotal,
        nextEpisodeAt = this.nextEpisodeAt,
        airedOn = this.airedOn?.date,
        releasedOn = this.releasedOn?.date,
        score = this.score,
        releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
    )
}
