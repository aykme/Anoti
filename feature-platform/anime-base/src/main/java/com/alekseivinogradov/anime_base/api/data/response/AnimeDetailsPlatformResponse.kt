package com.alekseivinogradov.anime_base.api.data.response

import com.squareup.moshi.Json

data class AnimeDetailsPlatformResponse(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val englishName: String?,
    @Json(name = "russian") val russianName: String?,
    @Json(name = "url") val pageUrl: String?,
    @Json(name = "image") val imageResponse: ImagePlatformResponse?,
    @Json(name = "episodes_aired") val episodesAired: Int?,
    @Json(name = "episodes") val episodesTotal: Int?,
    @Json(name = "next_episode_at") val nextEpisodeAt: String?,
    @Json(name = "aired_on") val airedOn: String?,
    @Json(name = "released_on") val releasedOn: String?,
    @Json(name = "score") val score: Float?,
    @Json(name = "status") val releaseStatus: String?,
    @Json(name = "kind") val kind: String?,
    @Json(name = "description") val description: String?,
)
