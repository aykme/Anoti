package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.remote.source.AnimeListSource
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnimeByIdUsecase(
    private val source: AnimeListSource
) {
    suspend fun execute(id: Int): CallResult<ListItemDomain> {
        return source.getItemById(id)
    }
}