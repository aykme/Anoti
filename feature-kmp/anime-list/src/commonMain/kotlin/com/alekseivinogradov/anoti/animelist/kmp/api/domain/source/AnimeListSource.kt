package com.alekseivinogradov.anoti.animelist.kmp.api.domain.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

interface AnimeListSource {

    suspend fun getOngoingList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>>

    suspend fun getAnnouncedList(
        page: Int,
        sort: SortData
    ): CallResult<List<ListItemDomain>>

    suspend fun getListBySearch(
        page: Int,
        search: String,
        sort: SortData
    ): CallResult<List<ListItemDomain>>

    suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain>
}
