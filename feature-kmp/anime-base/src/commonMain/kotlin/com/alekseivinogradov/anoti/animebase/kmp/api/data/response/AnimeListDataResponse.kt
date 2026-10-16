package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The `data` of [AnimeListResponse]: one entry per anime the server found. */
@Serializable
data class AnimeListDataResponse(
    @SerialName("animes") val animes: List<AnimeResponse>? = null
)
