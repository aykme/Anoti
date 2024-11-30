package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase

class ChangeAnimeDatabaseItemNewEpisodeStatusUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : ChangeAnimeDatabaseItemNewEpisodeStatusUsecase {
    override suspend fun execute(id: Int, isNewEpisode: Boolean) {
        repository.changeItemNewEpisodeStatus(
            id = id,
            isNewEpisode = isNewEpisode
        )
    }
}
