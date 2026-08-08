package com.alekseivinogradov.anoti.animebase.platform.api.data.response

import com.squareup.moshi.Json

data class ImagePlatformResponse(
    @param:Json(name = "original") val originalSizeUrl: String?,
    @param:Json(name = "preview") val previewSizeUrl: String?,
    @param:Json(name = "x96") val x96SizeUrl: String?,
    @param:Json(name = "x48") val x48SizeUrl: String?
)
