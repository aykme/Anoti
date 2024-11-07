package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsFlowUsecase
import kotlinx.coroutines.flow.Flow

class FetchAllDatabaseItemsFlowUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : FetchAllDatabaseItemsFlowUsecase {
    override fun execute(): Flow<List<AnimeDbDomain>> {
        return repository.getAllItemsFlow()
    }
}
