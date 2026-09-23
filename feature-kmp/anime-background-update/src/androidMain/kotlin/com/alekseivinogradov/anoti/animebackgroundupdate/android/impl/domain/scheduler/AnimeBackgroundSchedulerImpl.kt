package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.scheduler

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_PERIODIC_WORK_NAME
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler

/**
 * WorkManager-backed [AnimeBackgroundScheduler].
 *
 * The request replaces whatever is already scheduled. An app already on a phone carries the
 * schedule its previous release enqueued, and keeping that one would pin it to the old interval
 * and the old constraints for good.
 *
 * @param workManager the app's WorkManager handle, configured with the anime update worker
 *   factory.
 * @param animeUpdatePeriodicWork the periodic work request enqueued by [schedulePeriodicUpdate].
 */
class AnimeBackgroundSchedulerImpl(
    private val workManager: WorkManager,
    private val animeUpdatePeriodicWork: PeriodicWorkRequest
) : AnimeBackgroundScheduler {

    override fun schedulePeriodicUpdate() {
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = ANIME_UPDATE_PERIODIC_WORK_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = animeUpdatePeriodicWork
        )
    }
}
