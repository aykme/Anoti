package com.alekseivinogradov.anime_database.api.domain.usecase

import com.alekseivinogradov.anime_database.api.domain.model.AnimeDbDomain
import kotlinx.coroutines.flow.Flow

interface FetchAllAnimeDatabaseItemsFlowUsecase {
    fun execute(): Flow<List<AnimeDbDomain>>
}
