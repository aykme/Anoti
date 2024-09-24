package com.alekseivinogradov.database.api.data.model

data class AnimeDb(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val extraEpisodesInfo: String?,
    val score: Float?,
    val releaseStatus: ReleaseStatusDb,
    val episodesViewed: Int,
    val isNewEpisode: Boolean
)
