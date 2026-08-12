package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.scheduler

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.ANIME_UPDATE_PERIODIC_WORK_NAME
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler

/**
 * WorkManager-backed [AnimeBackgroundScheduler].
 *
 * @param workManager the app's WorkManager handle, already initialized with the anime update
 *   worker factory (see `AnimeBackgroundUpdatePlatformComponent`'s `provideWorkManager`).
 * @param animeUpdatePeriodicWork the periodic work request enqueued by [schedulePeriodicUpdate].
 */
class AnimeBackgroundSchedulerImpl(
    private val workManager: WorkManager,
    private val animeUpdatePeriodicWork: PeriodicWorkRequest
) : AnimeBackgroundScheduler {

    override fun schedulePeriodicUpdate() {
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = ANIME_UPDATE_PERIODIC_WORK_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = animeUpdatePeriodicWork
        )
    }
}
