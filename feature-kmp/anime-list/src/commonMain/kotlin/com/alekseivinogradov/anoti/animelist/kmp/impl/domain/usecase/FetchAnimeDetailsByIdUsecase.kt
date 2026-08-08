package com.alekseivinogradov.anoti.animelist.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animelist.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.source.AnimeListSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class FetchAnimeDetailsByIdUsecase(
    private val source: AnimeListSource
) {
    suspend fun execute(id: AnimeId): CallResult<ListItemDomain> {
        return source.getItemById(id)
    }
}
