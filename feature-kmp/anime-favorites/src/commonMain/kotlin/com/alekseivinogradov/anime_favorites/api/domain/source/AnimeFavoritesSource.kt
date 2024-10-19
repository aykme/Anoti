package com.alekseivinogradov.anime_favorites.api.domain.source

import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.network.api.domain.model.CallResult

interface AnimeFavoritesSource {

    suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain>
}
