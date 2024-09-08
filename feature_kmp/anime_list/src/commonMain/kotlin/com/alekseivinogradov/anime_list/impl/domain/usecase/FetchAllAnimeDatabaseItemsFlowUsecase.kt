package com.alekseivinogradov.anime_list.impl.domain.usecase

import com.alekseivinogradov.anime_list.api.data.local.repository.AnimeListDatabaseRepository
import com.alekseivinogradov.anime_list.api.domain.model.section.ListItemDomain
import kotlinx.coroutines.flow.Flow

class FetchAllAnimeDatabaseItemsFlowUsecase(
    private val repository: AnimeListDatabaseRepository
) {

    suspend fun execute(): Flow<List<ListItemDomain>> {
        return repository.getAllItemsFlow()
    }
}