package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase

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
