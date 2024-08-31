package com.alekseivinogradov.anime_network_base.api.data.remote.service

import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeShortPlatformResponse
import retrofit2.http.Query

interface ShikimoriApiServicePlatform {
    suspend fun getAnimeList(
        @Query("page") page: Int,
        @Query("limit") itemsPerPage: Int,
        @Query("status") releaseStatus: String?,
        @Query("order") sort: String?,
        @Query("ids") ids: String?,
        @Query("search") search: String?
    ): List<AnimeShortPlatformResponse>
}