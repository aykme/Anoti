package com.alekseivinogradov.anoti.animebase.kmp.impl.data.service

import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeListResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.GRAPHQL_APPEND_URL
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.ITEMS_PER_PAGE
import com.alekseivinogradov.anoti.network.kmp.api.domain.SHIKIMORI_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObjectBuilder
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
    ): AnimeListResponse {
        return httpClient.post("$SHIKIMORI_BASE_URL/$GRAPHQL_APPEND_URL") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("query", ANIME_LIST_QUERY)
                    putJsonObject("variables") {
                        put("page", page)
                        // The server answers two anime when no limit is given.
                        put("limit", ITEMS_PER_PAGE)
                        putIfPresent("order", sort)
                        putIfPresent("status", releaseStatus)
                        putIfPresent("search", search)
                        putIfPresent("ids", ids)
                    }
                }
            )
        }.body()
    }
}

// A null variable is not an absent one: a null order, for one, changes the ranking.
private fun JsonObjectBuilder.putIfPresent(key: String, value: String?) {
    if (value != null) put(key, value)
}

private const val ANIME_LIST_QUERY =
    $$"query Animes($page: PositiveInt!, $limit: PositiveInt!, $order: OrderEnum, " +
        $$"$status: AnimeStatusString, $search: String, $ids: String) { " +
        $$"animes(page: $page, limit: $limit, order: $order, status: $status, " +
        $$"search: $search, ids: $ids) { " +
        "id name poster { originalUrl } episodes episodesAired nextEpisodeAt " +
        "airedOn { date } releasedOn { date } score status } }"
