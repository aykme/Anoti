package com.alekseivinogradov.database.api.domain.model

import com.alekseivinogradov.celebrity.api.domain.AnimeId

/**
 * This is the entity of the Store (store for using database).
 * This is not the database entity, but just a mediator.
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
