package com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_list.impl.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeByIdUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeOngoingListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.InsertAnimeDatabaseItemUsecase

data class OngoingUsecases(
    val fetchAnimeOngoingListUsecase: FetchAnimeOngoingListUsecase,
    val fetchAnimeByIdUsecase: FetchAnimeByIdUsecase,
    val insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase,
    val deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase,
    val fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase
)