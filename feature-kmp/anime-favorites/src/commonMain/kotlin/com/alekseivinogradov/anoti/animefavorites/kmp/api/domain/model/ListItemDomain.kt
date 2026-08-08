package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * One anime in the user's favorites list.
 *
 * @param id anime's id.
 * @param name title.
 * @param imageUrl cover image, or null if unavailable.
 * @param episodesAired number of episodes aired so far, or null if unknown.
 * @param episodesTotal total number of episodes, or null if unknown.
 * @param nextEpisodeAt air date/time of the next episode, or null if none is scheduled.
 * @param airedOn date the anime started airing, or null if unknown.
 * @param releasedOn date the anime finished airing, or null if unknown/not yet released.
 * @param score community score, or null if unavailable.
 * @param releaseStatus current release status.
 * @param episodesViewed number of episodes the user has marked as viewed.
 * @param isNewEpisode whether a newly aired episode hasn't been viewed yet.
 */
data class ListItemDomain(
    val id: AnimeId,
    val name: String,
    val imageUrl: String?,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val nextEpisodeAt: String?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: Float?,
    val releaseStatus: ReleaseStatusDomain,
    val episodesViewed: Int,
    val isNewEpisode: Boolean
)
