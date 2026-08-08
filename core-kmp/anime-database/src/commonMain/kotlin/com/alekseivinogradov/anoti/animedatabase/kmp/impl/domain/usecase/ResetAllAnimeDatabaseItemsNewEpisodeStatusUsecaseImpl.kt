package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase

class ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase {
    override suspend fun execute() {
        repository.resetAllItemsNewEpisodeStatus()
    }
}
