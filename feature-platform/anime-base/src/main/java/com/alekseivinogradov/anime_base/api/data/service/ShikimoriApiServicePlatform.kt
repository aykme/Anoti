package com.alekseivinogradov.anime_base.api.data.service

import com.alekseivinogradov.anime_base.api.data.response.AnimeDetailsPlatformResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortPlatformResponse
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.celebrity.api.domain.ITEMS_PER_PAGE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ShikimoriApiServicePlatform {
    @GET(ANIME_LIST_APPEND_URL)
    suspend fun getAnimeList(
        @Query("page") page: Int,
        @Query("limit") itemsPerPage: Int = ITEMS_PER_PAGE,
        @Query("status") releaseStatus: String?,
        @Query("order") sort: String?,
        @Query("search") search: String?,
        @Query("ids") ids: String?,
    ): List<AnimeShortPlatformResponse>

    @GET("$ANIME_LIST_APPEND_URL/{id}")
    suspend fun getAnimeById(@Path("id") id: AnimeId): AnimeDetailsPlatformResponse
}
