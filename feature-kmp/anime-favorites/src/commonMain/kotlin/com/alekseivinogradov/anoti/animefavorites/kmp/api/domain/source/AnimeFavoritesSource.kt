package com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.source

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.model.ListItemDomain
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

interface AnimeFavoritesSource {

    suspend fun getItemById(id: AnimeId): CallResult<ListItemDomain>
}
