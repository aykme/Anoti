package com.alekseivinogradov.anime_background_update.api.domain.model

import com.alekseivinogradov.celebrity.api.domain.AnimeId

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
