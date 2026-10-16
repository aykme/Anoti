package com.alekseivinogradov.anoti.animelist.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.mapper.toAnimeResponses
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
    ): CallResult<List<ListItemDomain>> = getPage(
        page = page,
        releaseStatus = ReleaseStatusData.ONGOING.value,
        sort = sort,
        search = null
    )

    override suspend fun getAnnouncedList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>> = getPage(
        page = page,
        releaseStatus = ReleaseStatusData.ANNOUNCED.value,
        sort = sort,
        search = null
    )

    override suspend fun getListBySearch(
        page: Int,
        search: String,
        sort: SortData
    ): CallResult<List<ListItemDomain>> = getPage(
        page = page,
        releaseStatus = null,
        sort = sort,
        search = search
    )

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        return safeApi.call {
            service.getAnimeById(id).toListItemDomain()
        }
    }

    private suspend fun getPage(
        page: Int,
        releaseStatus: String?,
        sort: SortData,
        search: String?
    ): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = page,
                releaseStatus = releaseStatus,
                sort = sort.value,
                search = search,
                ids = null
            ).toAnimeResponses().mapNotNull { it.toListItemDomain() }
        }
    }
}
