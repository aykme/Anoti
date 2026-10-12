package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The `data` of [AnimePostersResponse]: one entry per anime the server found. */
@Serializable
data class AnimePostersDataResponse(
    @SerialName("animes") val animes: List<AnimePosterResponse>? = null
)
