package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase

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
