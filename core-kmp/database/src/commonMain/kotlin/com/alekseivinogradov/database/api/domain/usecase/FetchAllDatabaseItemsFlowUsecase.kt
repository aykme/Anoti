package com.alekseivinogradov.database.api.domain.usecase

import com.alekseivinogradov.database.api.domain.model.AnimeDbDomain
import kotlinx.coroutines.flow.Flow

interface FetchAllDatabaseItemsFlowUsecase {
    fun execute(): Flow<List<AnimeDbDomain>>
}
