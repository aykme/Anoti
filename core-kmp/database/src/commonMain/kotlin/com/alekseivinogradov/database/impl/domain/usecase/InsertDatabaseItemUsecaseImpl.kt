package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.InsertDatabaseItemUsecase

class InsertDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : InsertDatabaseItemUsecase {
    override suspend fun execute(anime: AnimeDbDomain) {
        repository.insert(anime)
    }
}
