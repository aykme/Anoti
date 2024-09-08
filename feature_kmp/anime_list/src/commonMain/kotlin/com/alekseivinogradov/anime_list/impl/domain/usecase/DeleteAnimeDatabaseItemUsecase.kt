package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.local.repository.AnimeListDatabaseRepository

class DeleteAnimeDatabaseItemUsecase(
    private val repository: AnimeListDatabaseRepository
) {

    suspend fun execute(id: Int) {
        repository.delete(id)
    }
}