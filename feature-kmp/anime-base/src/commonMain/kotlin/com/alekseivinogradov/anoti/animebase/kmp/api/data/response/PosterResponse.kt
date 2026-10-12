package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** @param originalUrl absolute URL of the poster as uploaded, the largest size. */
@Serializable
data class PosterResponse(
    @SerialName("originalUrl") val originalUrl: String? = null
)
