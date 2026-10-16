package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One entry of a GraphQL `errors` list.
 *
 * @param message the server's description.
 */
@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String? = null
)
