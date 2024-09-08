package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.local.repository.AnimeListDatabaseRepository
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain

class InsertAnimeDatabaseItemUsecase(
    private val repository: AnimeListDatabaseRepository
) {

    suspend fun execute(item: ListItemDomain) {
        repository.insert(item)
    }
}