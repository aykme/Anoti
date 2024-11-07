package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.UpdateDatabaseItemUsecase

class UpdateDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : UpdateDatabaseItemUsecase {
    override suspend fun execute(anime: AnimeDbDomain) {
        repository.update(anime)
    }
}
