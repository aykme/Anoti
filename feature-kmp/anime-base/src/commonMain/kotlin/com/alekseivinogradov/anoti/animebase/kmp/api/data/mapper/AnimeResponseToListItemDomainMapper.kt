package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.ImageResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL

/**
 * Resolves [imageResponse] to a full, absolute image URL (original size, falling back to
 * preview size), or null if neither is set.
 */
fun mapImageUrlDataToDomain(imageResponse: ImageResponse?): String? {
    val additionalImageUrl =
        imageResponse?.originalSizeUrl ?: imageResponse?.previewSizeUrl
    val fullImageUrl = additionalImageUrl?.let {
        SHIKIMORI_BASE_URL + additionalImageUrl
    }
    return fullImageUrl
}

/** Maps a raw Shikimori [releaseStatus] string to [ReleaseStatusDomain], defaulting to [ReleaseStatusDomain.UNKNOWN]. */
fun mapReleaseStatusDataToDomain(releaseStatus: String?): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusData.ONGOING.value -> ReleaseStatusDomain.ONGOING
        ReleaseStatusData.ANNOUNCED.value -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusData.RELEASED.value -> ReleaseStatusDomain.RELEASED
        else -> ReleaseStatusDomain.UNKNOWN
    }
}
