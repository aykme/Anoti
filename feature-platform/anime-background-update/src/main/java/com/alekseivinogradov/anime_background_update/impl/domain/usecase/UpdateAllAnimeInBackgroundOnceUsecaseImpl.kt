package com.alekseivinogradov.anime_background_update.impl.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anime_background_update.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase

class UpdateAllAnimeInBackgroundOnceUsecaseImpl(
    private val workManager: WorkManager,
    private val updateWork: OneTimeWorkRequest,
    private val uniqueWorkName: String
) : UpdateAllAnimeInBackgroundOnceUsecase {

    override fun execute() {
        workManager.enqueueUniqueWork(
            uniqueWorkName = uniqueWorkName,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = updateWork
        )
    }
}
