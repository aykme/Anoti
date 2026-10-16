package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.mapReleaseStatusDataToDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One anime from Shikimori GraphQL.
 *
 * @param id anime's id; GraphQL sends ids as strings.
 * @param englishName English title.
 * @param poster the poster, or null when the anime has none.
 * @param episodesTotal total number of episodes.
 * @param episodesAired number of episodes aired so far.
 * @param nextEpisodeAt air date/time of the next episode, or null if none is scheduled.
 * @param airedOn date the anime started airing.
 * @param releasedOn date the anime finished airing.
 * @param score Shikimori community score.
 * @param releaseStatus release status, as a raw Shikimori string (see
 * [mapReleaseStatusDataToDomain]).
 */
@Serializable
data class AnimeResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val englishName: String? = null,
    @SerialName("poster") val poster: PosterResponse? = null,
    @SerialName("episodes") val episodesTotal: Int? = null,
    @SerialName("episodesAired") val episodesAired: Int? = null,
    @SerialName("nextEpisodeAt") val nextEpisodeAt: String? = null,
    @SerialName("airedOn") val airedOn: IncompleteDateResponse? = null,
    @SerialName("releasedOn") val releasedOn: IncompleteDateResponse? = null,
    @SerialName("score") val score: Float? = null,
    @SerialName("status") val releaseStatus: String? = null
)
