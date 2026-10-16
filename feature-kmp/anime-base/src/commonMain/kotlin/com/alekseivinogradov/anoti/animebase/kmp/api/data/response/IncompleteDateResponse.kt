package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A date the server may know only in part.
 *
 * @param date ISO `yyyy-MM-dd`; a known year alone comes as its first day. Null when unknown.
 */
@Serializable
data class IncompleteDateResponse(
    @SerialName("date") val date: String? = null
)
