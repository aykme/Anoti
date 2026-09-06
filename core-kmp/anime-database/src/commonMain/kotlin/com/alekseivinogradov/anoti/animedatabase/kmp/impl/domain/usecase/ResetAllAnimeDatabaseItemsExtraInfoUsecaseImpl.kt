package com.alekseivinogradov.anoti.animedatabase.kmp.impl.domain.usecase

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.usecase.ResetAllAnimeDatabaseItemsExtraInfoUsecase

class ResetAllAnimeDatabaseItemsExtraInfoUsecaseImpl(
    private val repository: AnimeDatabaseRepository
) : ResetAllAnimeDatabaseItemsExtraInfoUsecase {
    override suspend fun execute() {
        repository.resetAllItemsExtraInfo()
    }
}
