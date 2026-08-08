package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

data class AnimeDetailsResponse(
    val id: AnimeId?,
    val englishName: String?,
    val russianName: String?,
    val pageUrl: String?,
    val imageResponse: ImageResponse?,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val nextEpisodeAt: String?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: Float?,
    val releaseStatus: String?,
    val kind: String?,
    val description: String?,
)
