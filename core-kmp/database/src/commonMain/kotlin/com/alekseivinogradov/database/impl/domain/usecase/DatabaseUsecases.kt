package com.alekseivinogradov.database.impl.domain.usecase

data class DatabaseUsecases(
    val fetchAllAnimeDatabaseItemsFlowUsecase: FetchAllAnimeDatabaseItemsFlowUsecase,
    val insertAnimeDatabaseItemUsecase: InsertAnimeDatabaseItemUsecase,
    val deleteAnimeDatabaseItemUsecase: DeleteAnimeDatabaseItemUsecase
)
