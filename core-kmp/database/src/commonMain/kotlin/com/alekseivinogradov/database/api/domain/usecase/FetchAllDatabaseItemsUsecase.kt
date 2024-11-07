package com.alekseivinogradov.database.api.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain

interface FetchAllDatabaseItemsUsecase {
    suspend fun execute(): List<AnimeDbDomain>
}
