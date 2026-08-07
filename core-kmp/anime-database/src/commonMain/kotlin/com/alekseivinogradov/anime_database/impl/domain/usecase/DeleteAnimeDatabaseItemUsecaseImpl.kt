package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.celebrity.api.domain.AnimeId

class DeleteAnimeDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : DeleteAnimeDatabaseItemUsecase {
    override suspend fun execute(id: AnimeId) {
        repository.delete(id)
    }
}
