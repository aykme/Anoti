package com.alekseivinogradov.anime_list.api.domain.source

import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.network.api.domain.model.CallResult

interface AnimeListSource {

    suspend fun getOngoingList(
        page: Int,
        sort: String
    ): CallResult<List<ListItemDomain>>

    suspend fun getAnnouncedList(
        page: Int,
        sort: String
    ): CallResult<List<ListItemDomain>>

    suspend fun getListBySearch(
        page: Int,
        search: String,
        sort: String
    ): CallResult<List<ListItemDomain>>

    suspend fun getItemById(id: Int): CallResult<ListItemDomain>
}
