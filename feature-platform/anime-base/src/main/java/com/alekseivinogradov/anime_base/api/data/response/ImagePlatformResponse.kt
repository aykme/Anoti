package com.alekseivinogradov.anime_base.api.data.response

import com.squareup.moshi.Json

data class ImagePlatformResponse(
    @Json(name = "original") val originalSizeUrl: String?,
    @Json(name = "preview") val previewSizeUrl: String?,
    @Json(name = "x96") val x96SizeUrl: String?,
    @Json(name = "x48") val x48SizeUrl: String?
)
