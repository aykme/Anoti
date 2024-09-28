package com.alekseivinogradov.anime_base.impl.remote

import com.alekseivinogradov.anime_base.api.data.remote.mapper.toKmp
import com.alekseivinogradov.anime_base.api.data.remote.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anime_base.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anime_base.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.domain.AnimeId

class ShikimoriApiServiceImpl(
    private val servicePlatform: ShikimoriApiServicePlatform
) : ShikimoriApiService {

    override suspend fun getAnimeList(
        page: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?,
    ): List<AnimeShortResponse> {
        return servicePlatform.getAnimeList(
            page = page,
            releaseStatus = releaseStatus,
            sort = sort,
            search = search,
            ids = ids
        ).map {
            it.toKmp()
        }
    }

    override suspend fun getAnimeById(id: AnimeId): AnimeDetailsResponse {
        return servicePlatform.getAnimeById(id).toKmp()
    }
}
