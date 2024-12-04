package com.alekseivinogradov.anime_database.api.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain

interface UpdateAnimeDatabaseItemUsecase {
    suspend fun execute(anime: AnimeDbDomain)
}
