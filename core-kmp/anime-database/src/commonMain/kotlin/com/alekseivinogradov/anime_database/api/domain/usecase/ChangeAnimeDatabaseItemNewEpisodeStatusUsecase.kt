package com.alekseivinogradov.anime_database.api.domain.usecase

interface ChangeAnimeDatabaseItemNewEpisodeStatusUsecase {
    suspend fun execute(id: Int, isNewEpisode: Boolean)
}
