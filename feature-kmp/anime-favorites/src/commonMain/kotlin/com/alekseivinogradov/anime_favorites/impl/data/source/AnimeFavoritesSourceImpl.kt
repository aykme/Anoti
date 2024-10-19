package com.alekseivinogradov.anime_favorites.impl.data.source

import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anime_base.api.domain.AnimeId
import com.alekseivinogradov.anime_favorites.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_favorites.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult

class AnimeFavoritesSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) : AnimeFavoritesSource {

    override suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain> {
        return safeApi.call {
            service.getAnimeById(id).toListItemDomain()
        }
    }
}
