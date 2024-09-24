package com.alekseivinogradov.anime_base.api.data.remote.response

data class ImageResponse(
    val originalSizeUrl: String?,
    val previewSizeUrl: String?,
    val x96SizeUrl: String?,
    val x48SizeUrl: String?
)
