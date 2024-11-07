package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsUsecase

class FetchAllDatabaseItemsUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : FetchAllDatabaseItemsUsecase {
    override suspend fun execute(): List<AnimeDbDomain> {
        return repository.getAllItems()
    }
}
