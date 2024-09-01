package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.model.section_content.ListItemDomain
import com.alekseivinogradov.anime_network_base.api.data.model.SortData
import com.alekseivinogradov.network.domain.CallResult

class FetchAnimeListBySearch(
    private val source: AnimeListSource
) {

    suspend fun execute(
        page: Int,
        itemsPerPage: Int,
        searchText: String
    ): CallResult<List<ListItemDomain>> {
        return source.getListBySearch(
            page = page,
            itemsPerPage = itemsPerPage,
            sort = SortData.POPULARITY.value,
            search = searchText
        )
    }
}