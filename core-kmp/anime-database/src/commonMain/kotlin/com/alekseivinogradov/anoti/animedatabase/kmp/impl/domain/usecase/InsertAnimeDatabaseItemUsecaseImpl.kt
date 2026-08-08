package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.InsertAnimeDatabaseItemUsecase

class InsertAnimeDatabaseItemUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : InsertAnimeDatabaseItemUsecase {
    override suspend fun execute(anime: AnimeDbDomain) {
        repository.insert(anime)
    }
}
