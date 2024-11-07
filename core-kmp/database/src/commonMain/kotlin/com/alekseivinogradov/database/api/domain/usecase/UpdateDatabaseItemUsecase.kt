package com.alekseivinogradov.database.api.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain

interface UpdateDatabaseItemUsecase {
    suspend fun execute(anime: AnimeDbDomain)
}
