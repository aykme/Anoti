package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class FetchAnimeListByIdsUsecase(
    private val source: AnimeBackgroundUpdateSource
) {
    suspend fun execute(ids: String): CallResult<List<ListItemDomain>> {
        return source.getListByIds(ids)
    }
}
