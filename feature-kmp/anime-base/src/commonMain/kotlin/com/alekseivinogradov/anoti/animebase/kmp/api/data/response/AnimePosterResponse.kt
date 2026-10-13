package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One anime's poster.
 *
 * @param id anime's id; GraphQL sends ids as strings.
 * @param poster the poster, or null when the anime has none.
 */
@Serializable
data class AnimePosterResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("poster") val poster: PosterResponse? = null
)
