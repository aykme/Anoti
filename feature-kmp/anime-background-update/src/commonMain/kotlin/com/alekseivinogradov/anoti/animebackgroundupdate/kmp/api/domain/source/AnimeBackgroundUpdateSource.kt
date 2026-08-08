package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.source

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

interface AnimeBackgroundUpdateSource {

    suspend fun getListByIds(ids: String): CallResult<List<ListItemDomain>>
}
