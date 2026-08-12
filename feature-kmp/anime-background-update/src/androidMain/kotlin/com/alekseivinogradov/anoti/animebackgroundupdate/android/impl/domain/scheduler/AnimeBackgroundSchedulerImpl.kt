package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.scheduler

import android.content.Context
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker.animeUpdatePeriodicWorkName
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler

/**
 * WorkManager-backed [AnimeBackgroundScheduler]. The app's manifest removes WorkManager's default
 * `androidx.startup` initializer, so [initializeWorkManager] must run once — before any
 * `WorkManager.getInstance` call — to install the custom [Configuration] carrying the anime
 * update worker factory.
 *
 * @param appContext the application context WorkManager is initialized and reached through.
 * @param workManagerConfiguration the custom WorkManager configuration to install.
 * @param animeUpdatePeriodicWork the periodic work request enqueued by [schedulePeriodicUpdate].
 */
class AnimeBackgroundSchedulerImpl(
    private val appContext: Context,
    private val workManagerConfiguration: Configuration,
    private val animeUpdatePeriodicWork: PeriodicWorkRequest
) : AnimeBackgroundScheduler {

    /** Installs the custom WorkManager [Configuration]. Call exactly once per process. */
    fun initializeWorkManager() {
        WorkManager.initialize(
            context = appContext,
            configuration = workManagerConfiguration
        )
    }

    override fun schedulePeriodicUpdate() {
        WorkManager.getInstance(context = appContext)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = animeUpdatePeriodicWorkName,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
                request = animeUpdatePeriodicWork
            )
    }
}
