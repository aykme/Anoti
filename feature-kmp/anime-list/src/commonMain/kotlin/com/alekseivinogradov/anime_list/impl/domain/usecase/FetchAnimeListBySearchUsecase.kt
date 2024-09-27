package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_base.api.data.model.SortData
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnimeListBySearchUsecase(
    private val source: AnimeListSource
) {

    suspend fun execute(
        page: Int,
        searchText: String
    ): CallResult<List<ListItemDomain>> {
        return source.getListBySearch(
            page = page,
            sort = SortData.SCORE.value,
            search = searchText
        )
    }
}
