package com.alekseivinogradov.database.api.domain.model

import com.alekseivinogradov.celebrity.api.domain.AnimeId

data class AnimeDb(
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
