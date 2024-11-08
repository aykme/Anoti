package com.alekseivinogradov.anime_background_update.impl.data.source

import com.alekseivinogradov.anime_background_update.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anime_background_update.api.domain.model.ListItemDomain
import com.alekseivinogradov.anime_background_update.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anime_base.api.data.service.ShikimoriApiService
import com.alekseivinogradov.celebrity.api.domain.FIRST_PAGE
import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult

class AnimeBackgroundUpdateSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi
) : AnimeBackgroundUpdateSource {

    override suspend fun getListByIds(ids: String): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            service.getAnimeList(
                page = FIRST_PAGE,
                ids = ids,
                releaseStatus = null,
                sort = null,
                search = null
            ).filter {
                it.id != null
            }.map {
                it.toListItemDomain()
            }
        }
    }
}
