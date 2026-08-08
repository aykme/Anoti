package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeDetailsResponse(
    @SerialName("id") val id: AnimeId?,
    @SerialName("name") val englishName: String?,
    @SerialName("russian") val russianName: String?,
    @SerialName("url") val pageUrl: String?,
    @SerialName("image") val imageResponse: ImageResponse?,
    @SerialName("episodes_aired") val episodesAired: Int?,
    @SerialName("episodes") val episodesTotal: Int?,
    @SerialName("next_episode_at") val nextEpisodeAt: String?,
    @SerialName("aired_on") val airedOn: String?,
    @SerialName("released_on") val releasedOn: String?,
    @SerialName("score") val score: Float?,
    @SerialName("status") val releaseStatus: String?,
    @SerialName("kind") val kind: String?,
    @SerialName("description") val description: String?,
)
