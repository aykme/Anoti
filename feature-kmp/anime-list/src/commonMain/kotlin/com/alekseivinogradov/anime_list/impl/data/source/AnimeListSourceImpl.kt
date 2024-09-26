package com.alekseivinogradov.anime_list.impl.data.source

import com.alekseivinogradov.anime_list.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult

class AnimeListSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) : AnimeListSource {

    override suspend fun getOngoingList(
        page: Int,
        itemsPerPage: Int,
        sort: String
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                itemsPerPage = itemsPerPage,
                releaseStatus = ReleaseStatusData.ONGOING.value,
                sort = sort,
                search = null,
                ids = null,
            ).filter {
                it.id != null
            }.map {
                it.toListItemDomain()
            }
        }
    }

    override suspend fun getAnnouncedList(
        page: Int,
        itemsPerPage: Int,
        sort: String
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                itemsPerPage = itemsPerPage,
                releaseStatus = ReleaseStatusData.ANNOUNCED.value,
                sort = sort,
                search = null,
                ids = null,
            ).filter {
                it.id != null
            }.map {
                it.toListItemDomain()
            }
        }
    }

    override suspend fun getListBySearch(
        page: Int,
        itemsPerPage: Int,
        search: String,
        sort: String
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                itemsPerPage = itemsPerPage,
                releaseStatus = null,
                sort = sort,
                search = search,
                ids = null,
            ).filter {
                it.id != null
            }.map {
                it.toListItemDomain()
            }
        }
    }

    override suspend fun getItemById(id: Int): CallResult<ListItemDomain> {
        return safeApi.call {
            service.getAnimeById(id).toListItemDomain()
        }
    }
}
