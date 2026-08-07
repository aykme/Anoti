package com.alekseivinogradov.anime_base.api.data.response

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.squareup.moshi.Json

data class AnimeDetailsPlatformResponse(
    @param:Json(name = "id") val id: AnimeId?,
    @param:Json(name = "name") val englishName: String?,
    @param:Json(name = "russian") val russianName: String?,
    @param:Json(name = "url") val pageUrl: String?,
    @param:Json(name = "image") val imageResponse: ImagePlatformResponse?,
    @param:Json(name = "episodes_aired") val episodesAired: Int?,
    @param:Json(name = "episodes") val episodesTotal: Int?,
    @param:Json(name = "next_episode_at") val nextEpisodeAt: String?,
    @param:Json(name = "aired_on") val airedOn: String?,
    @param:Json(name = "released_on") val releasedOn: String?,
    @param:Json(name = "score") val score: Float?,
    @param:Json(name = "status") val releaseStatus: String?,
    @param:Json(name = "kind") val kind: String?,
    @param:Json(name = "description") val description: String?,
)
