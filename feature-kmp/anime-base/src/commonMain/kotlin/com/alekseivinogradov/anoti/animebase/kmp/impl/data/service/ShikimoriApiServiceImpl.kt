package com.alekseivinogradov.anoti.animebase.kmp.impl.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ANIME_LIST_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ShikimoriApiServiceImpl(
    private val httpClient: HttpClient
) : ShikimoriApiService {

    override suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?
    ): List<AnimeShortResponse> {
        return httpClient.get("$SHIKIMORI_BASE_URL/$ANIME_LIST_APPEND_URL") {
            parameter("page", page)
            parameter("limit", ITEMS_PER_PAGE)
            parameter("status", releaseStatus)
            parameter("order", sort)
            parameter("search", search)
            parameter("ids", ids)
        }.body()
    }

    override suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse {
        return httpClient.get("$SHIKIMORI_BASE_URL/$ANIME_LIST_APPEND_URL/$id").body()
    }
}
