package com.alekseivinogradov.anoti.animefavorites.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class FetchAnimeDetailsByIdUsecase(
    private val source: AnimeFavoritesSource
) {
    suspend fun execute(id: AnimeId): CallResult<ListItemDomain> {
        return source.getItemById(id)
    }
}
