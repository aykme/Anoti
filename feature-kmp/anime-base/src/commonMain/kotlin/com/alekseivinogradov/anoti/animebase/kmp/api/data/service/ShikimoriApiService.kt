package com.alekseivinogradov.anoti.animebase.kmp.api.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

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
