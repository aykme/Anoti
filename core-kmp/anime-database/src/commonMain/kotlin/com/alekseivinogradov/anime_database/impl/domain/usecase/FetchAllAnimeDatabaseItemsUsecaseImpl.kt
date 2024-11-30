package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsUsecase

class FetchAllAnimeDatabaseItemsUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : FetchAllAnimeDatabaseItemsUsecase {
    override suspend fun execute(): List<AnimeDbDomain> {
        return repository.getAllItems()
    }
}
