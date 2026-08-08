package com.alekseivinogradov.anoti.animebase.platform.impl.data.service

import com.alekseivinogradov.anoti.animebase.platform.api.data.mapper.toKmp
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeDetailsResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.platform.api.data.service.ShikimoriApiServicePlatform
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

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
