package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full details for one anime, as returned by [ShikimoriApiService.getAnimeById].
 *
 * @param id anime's id.
 * @param englishName English title.
 * @param russianName Russian title.
 * @param pageUrl relative URL of the anime's Shikimori page.
 * @param imageResponse cover image at several sizes.
 * @param episodesAired number of episodes aired so far.
 * @param episodesTotal total number of episodes.
 * @param nextEpisodeAt air date/time of the next episode, or null if none is scheduled.
 * @param airedOn date the anime started airing.
 * @param releasedOn date the anime finished airing.
 * @param score Shikimori community score.
 * @param releaseStatus release status, as a raw Shikimori string (see
 * [mapReleaseStatusDataToDomain]).
 * @param kind format of the anime (e.g. "tv", "movie").
 * @param description synopsis.
 */
@Serializable
data class AnimeDetailsResponse(
    @SerialName("id") val id: AnimeId? = null,
    @SerialName("name") val englishName: String? = null,
    @SerialName("russian") val russianName: String? = null,
    @SerialName("url") val pageUrl: String? = null,
    @SerialName("image") val imageResponse: ImageResponse? = null,
    @SerialName("episodes_aired") val episodesAired: Int? = null,
    @SerialName("episodes") val episodesTotal: Int? = null,
    @SerialName("next_episode_at") val nextEpisodeAt: String? = null,
    @SerialName("aired_on") val airedOn: String? = null,
    @SerialName("released_on") val releasedOn: String? = null,
    @SerialName("score") val score: Float? = null,
    @SerialName("status") val releaseStatus: String? = null,
    @SerialName("kind") val kind: String? = null,
    @SerialName("description") val description: String? = null,
)
