package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase

interface ChangeAnimeDatabaseItemNewEpisodeStatusUsecase {
    suspend fun execute(id: Int, isNewEpisode: Boolean)
}
