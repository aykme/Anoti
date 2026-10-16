package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.animeId
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapPosterUrlDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeResponse

/**
 * One fetched anime as the update pass reads it, or null when the response carries no usable
 * id. Nothing downstream can match such an anime to a saved row.
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
