package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapImageUrlDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse

/**
 * One fetched anime as the update pass reads it, or null when the response carries no id.
 * Nothing downstream can match an id-less anime to a saved row.
 */
internal fun AnimeShortResponse.toListItemDomain(): ListItemDomain? {
    val animeId = this.id ?: return null

    return ListItemDomain(
        id = animeId,
        name = this.englishName ?: "",
        imageUrl = mapImageUrlDataToDomain(this.imageResponse),
        episodesAired = this.episodesAired,
        episodesTotal = this.episodesTotal,
        airedOn = this.airedOn,
        releasedOn = this.releasedOn,
        score = this.score,
        releaseStatus = mapReleaseStatusDataToDomain(this.releaseStatus)
    )
}
