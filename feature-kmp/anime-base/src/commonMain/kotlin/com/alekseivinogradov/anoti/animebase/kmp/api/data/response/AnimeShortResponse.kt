package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One anime list item, as returned by [ShikimoriApiService.getAnimeList].
 *
 * @param id anime's id.
 * @param englishName English title.
 * @param russianName Russian title.
 * @param pageUrl relative URL of the anime's Shikimori page.
 * @param imageResponse cover image at several sizes.
 * @param episodesAired number of episodes aired so far.
 * @param episodesTotal total number of episodes.
 * @param airedOn date the anime started airing.
 * @param releasedOn date the anime finished airing.
 * @param score Shikimori community score.
 * @param releaseStatus release status, as a raw Shikimori string (see
 * [mapReleaseStatusDataToDomain]).
 * @param kind format of the anime (e.g. "tv", "movie").
 */
@Serializable
data class AnimeShortResponse(
    @SerialName("id") val id: AnimeId? = null,
    @SerialName("name") val englishName: String? = null,
    @SerialName("russian") val russianName: String? = null,
    @SerialName("url") val pageUrl: String? = null,
    @SerialName("image") val imageResponse: ImageResponse? = null,
    @SerialName("episodes_aired") val episodesAired: Int? = null,
    @SerialName("episodes") val episodesTotal: Int? = null,
    @SerialName("aired_on") val airedOn: String? = null,
    @SerialName("released_on") val releasedOn: String? = null,
    @SerialName("score") val score: Float? = null,
    @SerialName("status") val releaseStatus: String? = null,
    @SerialName("kind") val kind: String? = null,
)
