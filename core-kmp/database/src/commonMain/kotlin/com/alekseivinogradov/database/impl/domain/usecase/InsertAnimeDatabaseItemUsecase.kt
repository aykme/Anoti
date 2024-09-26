package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.data.model.AnimeDb

class InsertAnimeDatabaseItemUsecase(
    private val repository: AnimeDatabaseRepository
) {

    suspend fun execute(item: AnimeDb) {
        repository.insert(item)
    }
}
