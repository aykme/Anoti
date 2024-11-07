package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.ChangeDatabaseItemNewEpisodeStatusUsecase

class ChangeDatabaseItemNewEpisodeStatusUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : ChangeDatabaseItemNewEpisodeStatusUsecase {
    override suspend fun execute(id: Int, isNewEpisode: Boolean) {
        repository.changeItemNewEpisodeStatus(
            id = id,
            isNewEpisode = isNewEpisode
        )
    }
}
