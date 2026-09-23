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
            // Not the end of the schedule: for periodic work WorkManager clears the attempt
            // count and waits out the interval as usual. Asking for a retry keeps that count
            // instead, and the backoff it drives takes the place of the interval, growing from
            // half a minute to five hours. For the one-off pass this ends the work, so the next
            // press starts a fresh one rather than being dropped into that same backoff.
            WorkResult.Error, null -> Result.failure()
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
            // Matched against the same source WorkManager stored the name from, so an obfuscated
            // build compares like with like. Null sends it to its own reflective fallback;
            // answering for a worker this module does not own would hand out the update worker
            // in its place.
            if (workerClassName != AnimeUpdateWorker::class.java.name) return null

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
 * that way is cut off wherever it happens to be. Finishing first leaves room to report a result
 * and to let the pages already applied stand.
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
