package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.celebrity.api.domain.AnimeId
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.DeleteDatabaseItemUsecase

class DeleteDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : DeleteDatabaseItemUsecase {
    override suspend fun execute(id: AnimeId) {
        repository.delete(id)
    }
}
