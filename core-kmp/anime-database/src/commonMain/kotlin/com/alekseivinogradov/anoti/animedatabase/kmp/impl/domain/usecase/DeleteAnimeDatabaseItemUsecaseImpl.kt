package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId

class DeleteAnimeDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : DeleteAnimeDatabaseItemUsecase {
    override suspend fun execute(id: AnimeId) {
        repository.delete(id)
    }
}
