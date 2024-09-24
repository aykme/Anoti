package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_base.api.data.model.SortData
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnimeAnnouncedListUsecase(
    private val source: AnimeListSource
) {

    suspend fun execute(
        page: Int,
        itemsPerPage: Int
    ): CallResult<List<ListItemDomain>> {
        return source.getAnnouncedList(
            page = page,
            itemsPerPage = itemsPerPage,
            sort = SortData.POPULARITY.value
        )
    }
}