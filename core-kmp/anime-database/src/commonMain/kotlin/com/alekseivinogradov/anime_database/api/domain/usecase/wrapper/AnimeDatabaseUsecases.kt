package com.alekseivinogradov.anime_database.api.domain.usecase.wrapper

import com.alekseivinogradov.anime_database.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anime_database.api.domain.usecase.UpdateAnimeDatabaseItemUsecase

data class AnimeDatabaseUsecases(
    val fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase,
    val insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase,
    val deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase,
    val resetAllAnimeDatabaseItemsNewEpisodeStatusUsecase:
    ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase,
    val changeAnimeDatabaseItemNewEpisodeStatusUsecase:
    ChangeAnimeDatabaseItemNewEpisodeStatusUsecase,
    val updateAnimeDatabaseItemUsecase: UpdateAnimeDatabaseItemUsecase
)
