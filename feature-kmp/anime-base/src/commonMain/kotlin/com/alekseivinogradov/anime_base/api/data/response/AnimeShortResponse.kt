package com.alekseivinogradov.anime_base.api.data.response

import com.alekseivinogradov.celebrity.api.domain.AnimeId

data class AnimeShortResponse(
    val id: AnimeId?,
    val englishName: String?,
    val russianName: String?,
    val pageUrl: String?,
    val imageResponse: ImageResponse?,
    val episodesAired: Int?,
    val episodesTotal: Int?,
    val airedOn: String?,
    val releasedOn: String?,
    val score: Float?,
    val releaseStatus: String?,
    val kind: String?,
)
