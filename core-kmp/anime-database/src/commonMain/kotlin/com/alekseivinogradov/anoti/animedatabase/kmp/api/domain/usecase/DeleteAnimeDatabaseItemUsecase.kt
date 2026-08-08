package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

interface DeleteAnimeDatabaseItemUsecase {
    suspend fun execute(id: AnimeId)
}
