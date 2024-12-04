package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import kotlinx.coroutines.flow.Flow

class FetchAllAnimeDatabaseItemsFlowUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : FetchAllAnimeDatabaseItemsFlowUsecase {
    override fun execute(): Flow<List<AnimeDbDomain>> {
        return repository.getAllItemsFlow()
    }
}
