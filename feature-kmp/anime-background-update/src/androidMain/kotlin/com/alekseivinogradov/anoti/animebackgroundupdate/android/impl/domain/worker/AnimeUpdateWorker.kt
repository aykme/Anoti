package com.alekseivinogradov.anoti.animebackgroundupdate.android.impl.domain.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import kotlinx.coroutines.withTimeoutOrNull
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.minutes

/**
 * Runs one [AnimeUpdateManager] pass over the saved anime library.
 *
 * @param animeUpdateManager does the pass.
 */
class AnimeUpdateWorker(
    appContext: Context,
    params: WorkerParameters,
    private val animeUpdateManager: AnimeUpdateManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val result = withTimeoutOrNull(UPDATE_PASS_BUDGET) { animeUpdateManager.update() }

        return when (result) {
            WorkResult.Success -> Result.success()
            // A page the server did not answer for, and a pass that ran out of budget, are both
            // worth another go. The pass applies each page as it arrives, so a retry picks up
            // where this run stopped instead of starting over.
            WorkResult.Error, null -> Result.retry()
        }
    }

    /**
     * Builds the update worker and declines anything else.
     *
     * @param animeUpdateManager handed to every worker this builds.
     */
    class Factory @Inject constructor(
        private val animeUpdateManager: AnimeUpdateManager
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? {
            // Null sends WorkManager to its own reflective fallback. Answering for a worker this
            // module does not own would hand out the update worker in its place.
            if (workerClassName != AnimeUpdateWorker::class.qualifiedName) return null

            return AnimeUpdateWorker(
                appContext = appContext,
                params = workerParameters,
                animeUpdateManager = animeUpdateManager
            )
        }
    }
}

/**
 * How long one pass may take. WorkManager stops a worker at ten minutes, and a pass stopped
 * that way loses the result it was about to report. Finishing first leaves room to report a
 * retry instead.
 */
private val UPDATE_PASS_BUDGET = 9.minutes

/**
 * What the update needs before it is worth waking the app for. Every page of the pass is a
 * network call, so a run without a connection can only fail.
 *
 * Nothing else is required: a new episode is worth knowing about on a phone that is in use and
 * not on a charger.
 */
val ANIME_UPDATE_WORK_CONSTRAINTS: Constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

const val ANIME_UPDATE_PERIODIC_WORK_NAME = "ANIME_UPDATE_PERIODIC_WORK"
const val ANIME_UPDATE_ONCE_WORK_NAME = "ANIME_UPDATE_ONCE_WORK"
