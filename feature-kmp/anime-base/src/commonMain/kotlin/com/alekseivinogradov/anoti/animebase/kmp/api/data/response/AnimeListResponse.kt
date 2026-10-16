package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shikimori GraphQL's answer to [ShikimoriApiService.getAnimeList].
 *
 * @param data the anime, or null when the query failed as a whole.
 * @param errors what went wrong, if anything; may come together with [data].
 */
@Serializable
data class AnimeListResponse(
    @SerialName("data") val data: AnimeListDataResponse? = null,
    @SerialName("errors") val errors: List<GraphQlErrorResponse>? = null
)
