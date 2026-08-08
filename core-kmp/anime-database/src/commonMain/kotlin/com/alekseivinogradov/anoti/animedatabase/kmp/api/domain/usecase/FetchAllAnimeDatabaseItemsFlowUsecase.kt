package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.model.AnimeDbDomain
import kotlinx.coroutines.flow.Flow

interface FetchAllAnimeDatabaseItemsFlowUsecase {
    fun execute(): Flow<List<AnimeDbDomain>>
}
