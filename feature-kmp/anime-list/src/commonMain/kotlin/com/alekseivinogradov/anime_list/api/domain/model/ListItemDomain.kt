package com.alekseivinogradov.anime_list.api.domain.model

import com.alekseivinogradov.anime_base.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.celebrity.api.domain.AnimeId

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
    val releaseStatus: ReleaseStatusDomain
)
