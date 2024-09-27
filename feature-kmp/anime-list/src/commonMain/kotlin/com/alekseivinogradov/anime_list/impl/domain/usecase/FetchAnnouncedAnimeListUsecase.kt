package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_base.api.data.model.SortData
import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnnouncedAnimeListUsecase(
    private val source: AnimeListSource
) {

    suspend fun execute(page: Int): CallResult<List<ListItemDomain>> {
        return source.getAnnouncedList(
            page = page,
            sort = SortData.POPULARITY.value
        )
    }
}
