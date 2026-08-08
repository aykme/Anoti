package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import kotlinx.coroutines.flow.Flow

class FetchAllAnimeDatabaseItemsFlowUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : FetchAllAnimeDatabaseItemsFlowUsecase {
    override fun execute(): Flow<List<AnimeDbDomain>> {
        return repository.getAllItemsFlow()
    }
}
