package com.alekseivinogradov.anoti.animebase.kmp.impl.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimePostersResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ANIME_LIST_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.GRAPHQL_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

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

    override suspend fun getAnimePosters(ids: List<AnimeId>): AnimePostersResponse {
        return httpClient.post("$SHIKIMORI_BASE_URL/$GRAPHQL_APPEND_URL") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("query", ANIME_POSTERS_QUERY)
                    putJsonObject("variables") {
                        put("ids", ids.joinToString(separator = ","))
                        // The server answers two anime when no limit is given.
                        put("limit", ids.size)
                    }
                }
            )
        }.body()
    }
}

private const val ANIME_POSTERS_QUERY =
    "query Posters(\$ids: String!, \$limit: PositiveInt!) { " +
        "animes(ids: \$ids, limit: \$limit) { id poster { originalUrl } } }"
