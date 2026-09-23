package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.OneShotCompletion
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval

private const val ANIME_UPDATE_TASK_IDENTIFIER = "com.alekseivinogradov.anoti.animeupdate.refresh"
private const val TAG = "AnimeBackgroundSchedulerImpl"

/**
 * [AnimeBackgroundScheduler] backed by `BGTaskScheduler`.
 *
 * The task identifier below must also be listed in the iOS app target's Info.plist under
 * `BGTaskSchedulerPermittedIdentifiers`.
 *
 * @param animeUpdateManager runs the update when the background task fires.
 * @param coroutineScope scope the update work runs in.
 */
@OptIn(ExperimentalForeignApi::class)
class AnimeBackgroundSchedulerImpl(
    private val animeUpdateManager: AnimeUpdateManager,
    private val coroutineScope: CoroutineScope
) : AnimeBackgroundScheduler {

    fun registerTaskHandler() {
        val registered = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = ANIME_UPDATE_TASK_IDENTIFIER,
            usingQueue = null
        ) { task ->
            if (task == null) return@registerForTaskWithIdentifier

            // Before any work: a task the system expires, or one whose pass ends badly, would
            // otherwise leave nothing submitted, and background refresh would stop for good.
            schedulePeriodicUpdate()

            val completion = OneShotCompletion { success: Boolean ->
                task.setTaskCompletedWithSuccess(success = success)
            }

            val job = coroutineScope.launch {
                try {
                    completion.complete(animeUpdateManager.update() == WorkResult.Success)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
                    println("$TAG: the update pass ended badly: $throwable")
                    completion.complete(success = false)
                }
            }

            task.expirationHandler = {
                job.cancel()
                completion.complete(success = false)
            }
        }

        if (!registered) {
            println("$TAG: $ANIME_UPDATE_TASK_IDENTIFIER is missing from the Info.plist")
        }
    }

    override fun schedulePeriodicUpdate() {
        val request = BGAppRefreshTaskRequest(identifier = ANIME_UPDATE_TASK_IDENTIFIER).apply {
            earliestBeginDate = NSDate().dateByAddingTimeInterval(EARLIEST_REFRESH_DELAY_SECONDS)
        }
        val submitted = BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)

        if (!submitted) {
            // Refused when background refresh is off for the app, or too many requests are
            // already pending. Either way there is no next pass until something submits one.
            println("$TAG: the next background refresh was refused")
        }
    }

    private companion object {
        private const val EARLIEST_REFRESH_DELAY_SECONDS = 15 * 60.0
    }
}
