package com.alekseivinogradov.anime_database.api.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain

interface InsertAnimeDatabaseItemUsecase {
    suspend fun execute(anime: AnimeDbDomain)
}
