package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.data.model.AnimeDb
import kotlinx.coroutines.flow.Flow

class FetchAllAnimeDatabaseItemsFlowUsecase(
    private val repository: AnimeDatabaseRepository
) {

    suspend fun execute(): Flow<List<AnimeDb>> {
        return repository.getAllItemsFlow()
    }
}
