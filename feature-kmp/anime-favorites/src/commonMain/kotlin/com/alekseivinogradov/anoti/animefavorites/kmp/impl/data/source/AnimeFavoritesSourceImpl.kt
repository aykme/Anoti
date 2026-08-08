package com.alekseivinogradov.anoti.animefavorites.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animefavorites.kmp.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source.AnimeFavoritesSource
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

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
