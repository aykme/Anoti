package com.alekseivinogradov.database.api.domain.model

data class AnimeDb(
    val id: Int,
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
