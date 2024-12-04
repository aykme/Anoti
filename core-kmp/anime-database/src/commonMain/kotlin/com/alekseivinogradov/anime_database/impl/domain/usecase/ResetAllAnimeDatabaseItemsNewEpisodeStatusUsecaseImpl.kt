package com.alekseivinogradov.anime_database.impl.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anime_database.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase

class ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase {
    override suspend fun execute() {
        repository.resetAllItemsNewEpisodeStatus()
    }
}
