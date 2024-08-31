package com.alekseivinogradov.anime_network_base.impl.remote

import com.alekseivinogradov.anime_network_base.api.data.remote.mapper.toKmp
import com.alekseivinogradov.anime_network_base.api.data.remote.response.AnimeShortResponse
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiService
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiServicePlatform

class ShikimoriApiServiceImpl(
    private val servicePlatform: ShikimoriApiServicePlatform
) : ShikimoriApiService {

    override suspend fun getAnimeList(
        page: Int,
        itemsPerPage: Int,
        releaseStatus: String?,
        sort: String?,
        search: String?,
        ids: String?,
    ): List<AnimeShortResponse> {
        return servicePlatform.getAnimeList(
            page = page,
            itemsPerPage = itemsPerPage,
            releaseStatus = releaseStatus,
            sort = sort,
            search = search,
            ids = ids
        ).map {
            it.toKmp()
        }
    }
}