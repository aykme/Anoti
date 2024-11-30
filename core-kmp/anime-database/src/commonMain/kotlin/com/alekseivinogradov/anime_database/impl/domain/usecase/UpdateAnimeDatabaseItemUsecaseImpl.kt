package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase

class UpdateAnimeDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : UpdateAnimeDatabaseItemUsecase {
    override suspend fun execute(anime: AnimeDbDomain) {
        repository.update(anime)
    }
}
