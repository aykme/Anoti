package com.alekseivinogradov.anime_background_update.api.domain.source

import com.alekseivinogradov.anime_background_update.api.domain.model.ListItemDomain
import com.alekseivinogradov.network.api.domain.model.CallResult

interface AnimeBackgroundUpdateSource {

    suspend fun getListByIds(ids: String): CallResult<List<ListItemDomain>>
}
