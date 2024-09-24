package com.alekseivinogradov.anime_base.api.data.remote.service

import com.alekseivinogradov.anime_base.api.data.remote.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.remote.response.AnimeShortResponse

const val ANIME_LIST_APPEND_URL = "api/animes"

interface ShikimoriApiService {

    suspend fun getAnimeList(
        page: Int,
        itemsPerPage: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): List<AnimeShortResponse>

    suspend fun getAnimeById(id: Int): AnimeDetailsResponse
}
