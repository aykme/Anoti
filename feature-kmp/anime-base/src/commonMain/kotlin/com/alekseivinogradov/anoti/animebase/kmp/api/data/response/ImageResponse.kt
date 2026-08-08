package com.alekseivinogradov.anoti.animebase.kmp.api.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An anime's cover image at several sizes; all relative URLs.
 *
 * @param originalSizeUrl full-size image.
 * @param previewSizeUrl preview-size image.
 * @param x96SizeUrl 96px image.
 * @param x48SizeUrl 48px image.
 */
@Serializable
data class ImageResponse(
    @SerialName("original") val originalSizeUrl: String? = null,
    @SerialName("preview") val previewSizeUrl: String? = null,
    @SerialName("x96") val x96SizeUrl: String? = null,
    @SerialName("x48") val x48SizeUrl: String? = null
)
