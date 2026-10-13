package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.data.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.data.mapper.toListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source.AnimeBackgroundUpdateSource
import com.alekseivinogradov.anoti.animebase.kmp.api.data.response.AnimeShortResponse
import com.alekseivinogradov.anoti.animebase.kmp.api.data.service.ShikimoriApiService
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.FIRST_PAGE
import com.alekseivinogradov.anoti.animebase.kmp.api.domain.source.AnimePosterSource
import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

class AnimeBackgroundUpdateSourceImpl(
    private val service: ShikimoriApiService,
    private val safeApi: SafeApi,
    private val posterSource: AnimePosterSource
) : AnimeBackgroundUpdateSource {

    override suspend fun getListByIds(ids: String): CallResult<List<ListItemDomain>> {
        return safeApi.call {
            val responses = service.getAnimeList(
                page = FIRST_PAGE,
                ids = ids,
                releaseStatus = null,
                sort = null,
                search = null
            )
            // Looked up for what the server sent, not for what was asked: it may leave ids out.
            val posterUrls = posterSource.getPosterUrls(
                responses.mapNotNull(AnimeShortResponse::id)
            )
            responses.mapNotNull { response: AnimeShortResponse ->
                response.toListItemDomain(posterUrl = posterUrls[response.id])
            }
        }
    }
}
