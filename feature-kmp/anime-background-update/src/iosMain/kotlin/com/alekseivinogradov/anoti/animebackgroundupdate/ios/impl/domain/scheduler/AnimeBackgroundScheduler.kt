package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval

private const val ANIME_UPDATE_TASK_IDENTIFIER = "com.alekseivinogradov.anoti.animeupdate.refresh"

/**
 * [AnimeBackgroundScheduler] backed by `BGTaskScheduler`.
 *
 * The task identifier below must also be listed in the iOS app target's Info.plist under
 * `BGTaskSchedulerPermittedIdentifiers`.
 *
 * @param animeUpdateManager runs the update when the background task fires.
 * @param coroutineScope scope the update work runs in.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalAtomicApi::class)
class AnimeBackgroundSchedulerImpl(
    private val animeUpdateManager: AnimeUpdateManager,
    private val coroutineScope: CoroutineScope
) : AnimeBackgroundScheduler {

    fun registerTaskHandler() {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = ANIME_UPDATE_TASK_IDENTIFIER,
            usingQueue = null
        ) { task ->
            if (task == null) return@registerForTaskWithIdentifier

            // Guards against a double completion: the coroutine and expirationHandler can race.
            val isCompleted = AtomicBoolean(false)
            fun completeOnce(success: Boolean) {
                if (isCompleted.compareAndSet(expectedValue = false, newValue = true)) {
                    task.setTaskCompletedWithSuccess(success = success)
                }
            }

            val job = coroutineScope.launch {
                try {
                    animeUpdateManager.update()
                    schedulePeriodicUpdate()
                    completeOnce(success = true)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Throwable) {
                    completeOnce(success = false)
                }
            }

            task.expirationHandler = {
                job.cancel()
                completeOnce(success = false)
            }
        }
    }

    override fun schedulePeriodicUpdate() {
        val request = BGAppRefreshTaskRequest(identifier = ANIME_UPDATE_TASK_IDENTIFIER).apply {
            earliestBeginDate = NSDate().dateByAddingTimeInterval(15 * 60.0)
        }
        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
    }
}
