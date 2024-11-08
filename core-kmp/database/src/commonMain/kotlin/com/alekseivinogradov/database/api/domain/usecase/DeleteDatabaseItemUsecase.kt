package com.alekseivinogradov.database.api.domain.usecase

import com.alekseivinogradov.celebrity.api.domain.AnimeId

interface DeleteDatabaseItemUsecase {
    suspend fun execute(id: AnimeId)
}
