package com.alekseivinogradov.database.api.domain.usecase

interface ChangeDatabaseItemNewEpisodeStatusUsecase {
    suspend fun execute(id: Int, isNewEpisode: Boolean)
}
