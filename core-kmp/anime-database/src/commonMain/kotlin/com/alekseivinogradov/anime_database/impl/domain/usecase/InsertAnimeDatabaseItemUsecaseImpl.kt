package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.InsertAnimeDatabaseItemUsecase

class InsertAnimeDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : InsertAnimeDatabaseItemUsecase {
    override suspend fun execute(anime: AnimeDbDomain) {
        repository.insert(anime)
    }
}
