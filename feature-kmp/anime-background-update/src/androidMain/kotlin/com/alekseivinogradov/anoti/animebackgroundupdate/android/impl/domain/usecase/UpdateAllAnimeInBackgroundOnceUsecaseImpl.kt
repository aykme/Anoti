package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase

/**
 * WorkManager-backed [UpdateAllAnimeInBackgroundOnceUsecase].
 *
 * @param workManager hands over the app's WorkManager. Taken as a function for the same reason
 * as in the scheduler: reaching for it inside the object graph's lock would deadlock against
 * WorkManager asking that graph for its configuration.
 * @param updateWork the work request enqueued by [execute].
 * @param uniqueWorkName the name a pass already waiting is recognized by.
 */
class UpdateAllAnimeInBackgroundOnceUsecaseImpl(
    private val workManager: () -> WorkManager,
    private val updateWork: OneTimeWorkRequest,
    private val uniqueWorkName: String
) : UpdateAllAnimeInBackgroundOnceUsecase {

    override fun execute() {
        workManager().enqueueUniqueWork(
            uniqueWorkName = uniqueWorkName,
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = updateWork
        )
    }
}
