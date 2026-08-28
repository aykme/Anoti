package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

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
