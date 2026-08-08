package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animelist.kmp.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class AnimeListSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) : AnimeListSource {

    override suspend fun getOngoingList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                releaseStatus = ReleaseStatusData.ONGOING.value,
                sort = sort.value,
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
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                releaseStatus = ReleaseStatusData.ANNOUNCED.value,
                sort = sort.value,
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
        sort: SortData
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                releaseStatus = null,
                sort = sort.value,
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
