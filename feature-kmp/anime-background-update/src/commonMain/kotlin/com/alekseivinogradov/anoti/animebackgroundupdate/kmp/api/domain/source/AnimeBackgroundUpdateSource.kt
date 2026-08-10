package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/**
 * Fetches fresh anime data by their IDs from the Shikimori API.
 */
interface AnimeBackgroundUpdateSource {

    /**
     * Fetches anime list by their IDs.
     *
     * @param ids comma-separated anime IDs to fetch.
     * @return the result containing a list of fetched anime or an error.
     */
    suspend fun getListByIds(ids: String): CallResult<List<ListItemDomain>>
}
