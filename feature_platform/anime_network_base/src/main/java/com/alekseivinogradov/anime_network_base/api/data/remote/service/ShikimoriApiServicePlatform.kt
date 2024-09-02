package com.alekseivinogradov.anime_network_base.api.data.remote.service

import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeShortPlatformResponse
import com.alekseivinogradov.network.impl.data.remote.shikimoriRetrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface ShikimoriApiServicePlatform {
    companion object {
        val instance: ShikimoriApiServicePlatform by lazy {
            shikimoriRetrofit.create(ShikimoriApiServicePlatform::class.java)
        }
    }

    @GET(ANIME_LIST_APPEND_URL)
    suspend fun getAnimeList(
        @Query("page") page: Int,
        @Query("limit") itemsPerPage: Int,
        @Query("status") releaseStatus: String?,
        @Query("order") sort: String?,
        @Query("search") search: String?,
        @Query("ids") ids: String?,
    ): List<AnimeShortPlatformResponse>
}