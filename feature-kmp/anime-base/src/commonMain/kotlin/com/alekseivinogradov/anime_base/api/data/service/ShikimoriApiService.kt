package com.alekseivinogradov.anime_base.api.data.service

import com.alekseivinogradov.anime_base.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anime_base.api.domain.AnimeId

const val ANIME_LIST_APPEND_URL = "api/animes"

interface ShikimoriApiService {

    suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): List<AnimeShortResponse>

    suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse
}
