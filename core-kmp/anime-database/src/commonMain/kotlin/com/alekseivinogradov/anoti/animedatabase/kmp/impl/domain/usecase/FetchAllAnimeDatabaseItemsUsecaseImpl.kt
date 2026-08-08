package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase

class FetchAllAnimeDatabaseItemsUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : FetchAllAnimeDatabaseItemsUsecase {
    override suspend fun execute(): List<AnimeDbDomain> {
        return repository.getAllItems()
    }
}
