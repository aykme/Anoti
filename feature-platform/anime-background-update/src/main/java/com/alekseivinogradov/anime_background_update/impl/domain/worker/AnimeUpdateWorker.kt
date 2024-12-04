package com.alekseivinogradov.anime_background_update.impl.domain.worker

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.alekseivinogradov.anime_background_update.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anime_background_update.api.domain.model.WorkResult
import javax.inject.Inject

@WorkerThread
class AnimeUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
    private val animeUpdateManager: AnimeUpdateManager
) : CoroutineWorker(appContext, params) {

    companion object {
        const val animeUpdatePeriodicWorkName = "ANIME_UPDATE_PERIODIC_WORK"
        const val animeUpdateOnceWorkName = "ANIME_UPDATE_ONCE_WORK"
    }

    override suspend fun doWork(): Result {
        return when (animeUpdateManager.update()) {
            WorkResult.Success -> Result.success()
            WorkResult.Error -> Result.failure()
        }
    }

    class Factory @Inject constructor(
        private val animeUpdateManager: AnimeUpdateManager
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return AnimeUpdateWorker(
                appContext = appContext,
                params = workerParameters,
                animeUpdateManager = animeUpdateManager
            )
        }
    }
}
