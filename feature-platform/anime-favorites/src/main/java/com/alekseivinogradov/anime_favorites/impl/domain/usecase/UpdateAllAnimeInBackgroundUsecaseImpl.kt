package com.alekseivinogradov.anime_favorites.impl.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anime_favorites.api.domain.usecase.UpdateAllAnimeInBackgroundUsecase

internal class UpdateAllAnimeInBackgroundUsecaseImpl(
    private val workManager: WorkManager,
    private val updateWork: OneTimeWorkRequest,
    private val uniqueWorkName: String
) : UpdateAllAnimeInBackgroundUsecase {

    override fun execute() {
        workManager.enqueueUniqueWork(
            /* uniqueWorkName = */ uniqueWorkName,
            /* existingWorkPolicy = */ ExistingWorkPolicy.KEEP,
            /* work = */ updateWork
        )
    }
}
