package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebase.kmp.api.data.model.SortData
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class FetchOngoingAnimeListUsecase(
    private val source: AnimeListSource
) {

    suspend fun execute(page: Int): CallResult<List<ListItemDomain>> {
        return source.getOngoingList(
            page = page,
            sort = SortData.SCORE
        )
    }
}
