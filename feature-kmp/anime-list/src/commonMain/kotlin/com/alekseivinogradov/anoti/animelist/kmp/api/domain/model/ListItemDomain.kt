package com.alekseivinogradov.anoti.animelist.kmp.api.domain.model

import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

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
