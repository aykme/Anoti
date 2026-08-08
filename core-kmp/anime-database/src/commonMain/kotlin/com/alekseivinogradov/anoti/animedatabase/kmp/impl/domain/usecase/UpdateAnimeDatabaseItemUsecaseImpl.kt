package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase

class UpdateAnimeDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : UpdateAnimeDatabaseItemUsecase {
    override suspend fun execute(anime: AnimeDbDomain) {
        repository.update(anime)
    }
}
