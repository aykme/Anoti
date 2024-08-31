package com.alekseivinogradov.anime_network_base.api.data.remote.service

interface ShikimoriApiService {

    suspend fun getAnimeList(
        page: Int,
        itemsPerPage: Int,
        releaseStatus: String?,
        sort: String?,
        ids: String?,
        search: String?
    )
}