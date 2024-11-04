package com.alekseivinogradov.anime_background_update.impl.domain.usecase

import com.alekseivinogradov.anime_background_update.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_background_update.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnimeListByIdsUsecase(
    private val source: AnimeBackgroundUpdateSource
) {
    suspend fun execute(ids: String): CallResult<List<ListItemDomain>> {
        return source.getListByIds(ids)
    }
}
