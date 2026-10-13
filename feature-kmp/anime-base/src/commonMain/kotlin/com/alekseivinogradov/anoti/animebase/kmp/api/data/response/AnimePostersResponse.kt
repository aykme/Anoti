package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shikimori GraphQL's answer to [ShikimoriApiService.getAnimePosters].
 *
 * @param data the posters, or null when the query failed as a whole.
 * @param errors what went wrong, if anything; may come together with [data].
 */
@Serializable
data class AnimePostersResponse(
    @SerialName("data") val data: AnimePostersDataResponse? = null,
    @SerialName("errors") val errors: List<GraphQlErrorResponse>? = null
)
