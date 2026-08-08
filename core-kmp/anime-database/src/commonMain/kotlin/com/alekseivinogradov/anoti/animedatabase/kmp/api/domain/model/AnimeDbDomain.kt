package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

/**
 * A saved anime entry, as read from and written to [AnimeDatabaseStore][com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore].
 * Not the database entity itself, just the mediator type the store/usecases operate on.
 *
 * @param id id of the anime.
 * @param imageUrl cover image URL, or null if unknown.
 * @param name display name of the anime.
 * @param episodesAired number of episodes aired so far, or null if unknown.
 * @param episodesTotal total number of episodes, or null if not yet announced.
 * @param nextEpisodeAt air date/time of the next episode, or null if none is scheduled.
 * @param airedOn date the anime started airing, or null if unknown.
 * @param releasedOn date the anime finished airing, or null if it hasn't finished.
 * @param score Shikimori community score, or null if unavailable.
 * @param releaseStatus current release status.
 * @param episodesViewed number of episodes the user has watched.
 * @param isNewEpisode whether a new episode has aired since the user last checked.
 */
data class AnimeDbDomain(
    val id: AnimeId,
    val imageUrl: String?,
    val name: String,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val nextEpisodeAt: String?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: Float?,
    val releaseStatus: ReleaseStatusDb,
    val episodesViewed: Int,
    val isNewEpisode: Boolean
)
