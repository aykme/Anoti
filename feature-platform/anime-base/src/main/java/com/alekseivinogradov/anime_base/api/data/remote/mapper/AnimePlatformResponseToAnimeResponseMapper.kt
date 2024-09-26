package com.alekseivinogradov.anime_base.api.data.remote.mapper

import com.alekseivinogradov.anime_base.api.data.remote.response.AnimeDetailsPlatformResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.remote.response.AnimeShortPlatformResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anime_base.api.data.remote.response.ImagePlatformResponse
import com.alekseivinogradov.anime_base.api.data.response.ImageResponse

internal fun AnimeShortPlatformResponse.toKmp() = AnimeShortResponse(
    id = this.id,
    englishName = this.englishName,
    russianName = this.russianName,
    pageUrl = this.pageUrl,
    imageResponse = this.imageResponse?.toKmp(),
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = this.releaseStatus,
    kind = this.kind,
)

internal fun AnimeDetailsPlatformResponse.toKmp() = AnimeDetailsResponse(
    id = this.id,
    englishName = this.englishName,
    russianName = this.russianName,
    pageUrl = this.pageUrl,
    imageResponse = this.imageResponse?.toKmp(),
    episodesAired = this.episodesAired,
    episodesTotal = this.episodesTotal,
    nextEpisodeAt = this.nextEpisodeAt,
    airedOn = this.airedOn,
    releasedOn = this.releasedOn,
    score = this.score,
    releaseStatus = this.releaseStatus,
    kind = this.kind,
    description = this.description
)

private fun ImagePlatformResponse.toKmp() = ImageResponse(
    originalSizeUrl = this.originalSizeUrl,
    previewSizeUrl = this.previewSizeUrl,
    x96SizeUrl = this.x96SizeUrl,
    x48SizeUrl = this.x48SizeUrl
)
