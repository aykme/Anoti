package com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.ImageResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.model.ReleaseStatusDomain
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL

fun mapImageUrlDataToDomain(imageResponse: ImageResponse?): String? {
    val additionalImageUrl =
        imageResponse?.originalSizeUrl ?: imageResponse?.previewSizeUrl
    val fullImageUrl = additionalImageUrl?.let {
        SHIKIMORI_BASE_URL + additionalImageUrl
    }
    return fullImageUrl
}

fun mapReleaseStatusDataToDomain(releaseStatus: String?): ReleaseStatusDomain {
    return when (releaseStatus) {
        ReleaseStatusData.ONGOING.value -> ReleaseStatusDomain.ONGOING
        ReleaseStatusData.ANNOUNCED.value -> ReleaseStatusDomain.ANNOUNCED
        ReleaseStatusData.RELEASED.value -> ReleaseStatusDomain.RELEASED
        else -> ReleaseStatusDomain.UNKNOWN
    }
}
