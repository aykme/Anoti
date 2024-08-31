package com.alekseivinogradov.anime_network_base.impl.remote

import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiService
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiServicePlatform

class ShikimoriApiServiceImpl(
    val servicePlatform: ShikimoriApiServicePlatform
) : ShikimoriApiService {

    override suspend fun getAnimeList(
        page: Int,
        itemsPerPage: Int,
        releaseStatus: String?,
        sort: String?,
        ids: String?,
        search: String?
    ) {
        servicePlatform.getAnimeList(
            page = page,
            itemsPerPage = itemsPerPage,
            releaseStatus = releaseStatus,
            sort = sort,
            ids = ids,
            search = search
        )
    }
}