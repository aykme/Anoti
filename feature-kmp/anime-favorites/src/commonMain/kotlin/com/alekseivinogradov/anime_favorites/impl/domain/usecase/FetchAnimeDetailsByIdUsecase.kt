package com.alekseivinogradov.anime_favorites.impl.domain.usecase

import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.network.api.domain.model.CallResult

class FetchAnimeDetailsByIdUsecase(
    private val source: AnimeFavoritesSource
) {
    suspend fun execute(id: AnimeId): CallResult<ListItemDomain> {
        return source.getItemById(id)
    }
}
