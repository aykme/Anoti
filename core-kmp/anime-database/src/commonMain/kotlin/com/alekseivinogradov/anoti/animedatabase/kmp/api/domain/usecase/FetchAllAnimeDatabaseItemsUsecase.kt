package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain

interface FetchAllAnimeDatabaseItemsUsecase {
    suspend fun execute(): List<AnimeDbDomain>
}
