package com.alekseivinogradov.anime_database.api.domain.usecase

import com.alekseivinogradov.celebrity.api.domain.AnimeId

interface DeleteAnimeDatabaseItemUsecase {
    suspend fun execute(id: AnimeId)
}
