package com.alekseivinogradov.anime_list.impl.domain.usecase.wrapper

import com.alekseivinogradov.anime_list.impl.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.FetchAnimeAnnouncedListUsecase
import com.alekseivinogradov.anime_list.impl.domain.usecase.InsertAnimeDatabaseItemUsecase

data class AnnouncedUsecases(
    val fetchAnimeAnnouncedListUsecase: FetchAnimeAnnouncedListUsecase,
    val insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase,
    val deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase,
    val fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase
)