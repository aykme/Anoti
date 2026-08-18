package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.usecase

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * iOS [UpdateAllAnimeInBackgroundOnceUsecase]: runs one update pass in [coroutineScope].
 *
 * `BGTaskScheduler` is deliberately not used here — `BGAppRefreshTask` never fires while the app
 * is in the foreground, and this usecase is triggered by a tap on the favorites screen.
 *
 * A pass already in flight is kept rather than restarted, mirroring Android's
 * `ExistingWorkPolicy.KEEP`.
 */
@OptIn(ExperimentalAtomicApi::class)
class UpdateAllAnimeInBackgroundOnceUsecaseImpl(
    private val animeUpdateManager: AnimeUpdateManager,
    private val coroutineScope: CoroutineScope
) : UpdateAllAnimeInBackgroundOnceUsecase {

    private val runningJob = AtomicReference<Job?>(null)

    override fun execute() {
        val newJob = coroutineScope.launch(start = CoroutineStart.LAZY) {
            animeUpdateManager.update()
        }
        while (true) {
            val currentJob = runningJob.load()
            if (currentJob?.isActive == true) {
                newJob.cancel()
                return
            }
            if (runningJob.compareAndSet(currentJob, newJob)) {
                newJob.start()
                return
            }
        }
    }
}
