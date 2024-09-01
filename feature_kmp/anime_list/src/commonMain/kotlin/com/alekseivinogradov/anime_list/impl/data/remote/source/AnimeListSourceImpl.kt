package com.alekseivinogradov.anime_list.impl.data.remote.source

import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.anime_list.impl.data.remote.mapper.toAnnouncedListItem
import com.alekseivinogradov.anime_list.impl.data.remote.mapper.toOngoingListItem
import com.alekseivinogradov.anime_list.impl.data.remote.mapper.toSearchListItem
import com.alekseivinogradov.anime_network_base.api.data.model.ReleaseStatusData
import com.alekseivinogradov.anime_network_base.api.data.remote.service.ShikimoriApiService
import com.alekseivinogradov.network.domain.model.CallResult
import com.alekseivinogradov.network.data.remote.SafeApi

internal class AnimeListSourceImpl(
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
            ).map {
                it.toOngoingListItem()
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
            ).map {
                it.toAnnouncedListItem()
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
            ).map {
                it.toSearchListItem()
            }
        }
    }
}