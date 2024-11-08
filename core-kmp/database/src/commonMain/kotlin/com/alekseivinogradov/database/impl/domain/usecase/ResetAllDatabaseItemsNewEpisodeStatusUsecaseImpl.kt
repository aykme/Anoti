package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.usecase.ResetAllDatabaseItemsNewEpisodeStatusUsecase

class ResetAllDatabaseItemsNewEpisodeStatusUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : ResetAllDatabaseItemsNewEpisodeStatusUsecase {
    override suspend fun execute() {
        repository.resetAllItemsNewEpisodeStatus()
    }
}
