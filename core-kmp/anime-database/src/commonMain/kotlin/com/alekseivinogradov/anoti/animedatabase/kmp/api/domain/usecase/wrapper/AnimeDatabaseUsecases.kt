package com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.wrapper

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ChangeAnimeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.DeleteAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.FetchAllAnimeDatabaseItemsFlowUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.InsertAnimeDatabaseItemUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.UpdateAnimeDatabaseItemUsecase

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
