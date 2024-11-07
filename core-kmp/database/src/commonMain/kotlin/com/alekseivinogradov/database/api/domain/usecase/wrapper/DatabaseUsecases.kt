package com.alekseivinogradov.database.api.domain.usecase.wrapper

import com.alekseivinogradov.database.api.domain.usecase.ChangeDatabaseItemNewEpisodeStatusUsecase
import com.alekseivinogradov.database.api.domain.usecase.DeleteDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.FetchAllDatabaseItemsFlowUsecase
import com.alekseivinogradov.database.api.domain.usecase.InsertDatabaseItemUsecase
import com.alekseivinogradov.database.api.domain.usecase.ResetAllDatabaseItemsNewEpisodeStatusUsecase
import com.alekseivinogradov.database.api.domain.usecase.UpdateDatabaseItemUsecase

data class DatabaseUsecases(
    val fetchAllDatabaseItemsFlowUsecase: FetchAllDatabaseItemsFlowUsecase,
    val insertDatabaseItemUsecase: InsertDatabaseItemUsecase,
    val deleteDatabaseItemUsecase: DeleteDatabaseItemUsecase,
    val resetAllDatabaseItemsNewEpisodeStatusUsecase: ResetAllDatabaseItemsNewEpisodeStatusUsecase,
    val changeDatabaseItemNewEpisodeStatusUsecase: ChangeDatabaseItemNewEpisodeStatusUsecase,
    val updateDatabaseItemUsecase: UpdateDatabaseItemUsecase
)
