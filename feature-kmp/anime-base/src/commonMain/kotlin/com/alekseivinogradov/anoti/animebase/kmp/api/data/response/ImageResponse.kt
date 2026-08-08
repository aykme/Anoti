package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageResponse(
    @SerialName("original") val originalSizeUrl: String?,
    @SerialName("preview") val previewSizeUrl: String?,
    @SerialName("x96") val x96SizeUrl: String?,
    @SerialName("x48") val x48SizeUrl: String?
)
