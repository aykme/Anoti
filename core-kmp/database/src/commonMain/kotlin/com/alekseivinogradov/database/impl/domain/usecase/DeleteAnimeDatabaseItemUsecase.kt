package com.alekseivinogradov.database.impl.domain.usecase

import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository

class DeleteAnimeDatabaseItemUsecase(
    private val repository: AnimeDatabaseRepository
) {

    suspend fun execute(id: Int) {
        repository.delete(id)
    }
}
