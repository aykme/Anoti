package com.alekseivinogradov.anime_list.impl.data.source

import com.alekseivinogradov.anime_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_list.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult

class AnimeListSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) : AnimeListSource {

    override suspend fun getOngoingList(
        page: Int,
        sort: String
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
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
        sort: String
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
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
        search: String,
        sort: String
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
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

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        return safeApi.call {
            service.getAnimeById(id).toListItemDomain()
        }
    }
}
