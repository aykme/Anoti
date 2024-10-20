package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_list.api.domain.source.AnimeListSource
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnimeDetailsByIdUsecase(
    private val source: AnimeListSource
) {
    suspend fun execute(id: AnimeId): CallResult<ListItemDomain> {
        return source.getItemById(id)
    }
}
