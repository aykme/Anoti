package com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_list.impl.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeListBySearchUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.InsertAnimeDatabaseItemUsecase

data class SearchUsecases(
    val fetchAnimeListBySearchUsecase: FetchAnimeListBySearchUsecase,
    val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase,
    val insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase,
    val deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase,
    val fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase
)