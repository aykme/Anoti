package com.alekseivinogradov.anime_list.api.data.remote.source

import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.network.api.domain.model.CallResult

interface AnimeListSource {

    suspend fun getOngoingList(
        page: Int,
        itemsPerPage: Int,
        sort: String
    ): CallResult<List<ListItemDomain>>

    suspend fun getAnnouncedList(
        page: Int,
        itemsPerPage: Int,
        sort: String
    ): CallResult<List<ListItemDomain>>

    suspend fun getListBySearch(
        page: Int,
        itemsPerPage: Int,
        search: String,
        sort: String
    ): CallResult<List<ListItemDomain>>

    suspend fun getItemById(id: Int): CallResult<ListItemDomain>
}