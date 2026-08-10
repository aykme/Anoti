package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * Domain representation of an anime item retrieved during a background update.
 *
 * @param id unique anime identifier.
 * @param name anime name.
 * @param imageUrl URL to the anime's cover image.
 * @param episodesAired number of episodes that have aired.
 * @param episodesTotal total planned episodes.
 * @param airedOn when the anime first aired.
 * @param releasedOn when the anime was released.
 * @param score average rating.
 * @param releaseStatus current release status (ongoing, announced, released, etc.).
 */
data class ListItemDomain(
    val id: AnimeId,
    val name: String,
    val imageUrl: String?,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: Float?,
    val releaseStatus: ReleaseStatusDomain
)
