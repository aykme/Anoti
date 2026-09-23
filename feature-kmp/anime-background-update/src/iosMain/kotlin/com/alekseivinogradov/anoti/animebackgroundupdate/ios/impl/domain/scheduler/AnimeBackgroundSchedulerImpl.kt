package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.BackgroundRefreshPass
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.BackgroundRefreshTask
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateByAddingTimeInterval
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private const val ANIME_UPDATE_TASK_IDENTIFIER = "com.alekseivinogradov.anoti.animeupdate.refresh"
private const val TAG = "AnimeBackgroundSchedulerImpl"

// The platform kills the app on a second registration of one identifier, and a registered
// handler outlives whatever registered it, so the guard belongs to the process rather than to
// an instance.
@OptIn(ExperimentalAtomicApi::class)
private val taskHandlerRegistered = AtomicBoolean(false)

/**
 * [AnimeBackgroundScheduler] backed by `BGTaskScheduler`.
 *
 * The task identifier below must also be listed in the iOS app target's Info.plist under
 * `BGTaskSchedulerPermittedIdentifiers`.
 *
 * @param animeUpdateManager runs the update when the background task fires.
 * @param coroutineScope scope the update work runs in.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalAtomicApi::class, BetaInteropApi::class)
class AnimeBackgroundSchedulerImpl(
    animeUpdateManager: AnimeUpdateManager,
    coroutineScope: CoroutineScope
) : AnimeBackgroundScheduler {

    private val refreshPass = BackgroundRefreshPass(
        animeUpdateManager = animeUpdateManager,
        coroutineScope = coroutineScope
    )

    /**
     * Takes on the background task the platform launches this app for. It has to run before the
     * app finishes launching, and it takes effect once per process however often it is called.
     */
    fun registerTaskHandler() {
        if (!taskHandlerRegistered.compareAndSet(expectedValue = false, newValue = true)) return

        val registered = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = ANIME_UPDATE_TASK_IDENTIFIER,
            usingQueue = null
        ) { task -> task?.let(::runUpdateFor) }

        if (!registered) {
            taskHandlerRegistered.store(false)
            println("$TAG: $ANIME_UPDATE_TASK_IDENTIFIER is missing from the Info.plist")
        }
    }

    override fun schedulePeriodicUpdate() {
        BGTaskScheduler.sharedScheduler.getPendingTaskRequestsWithCompletionHandler { pending ->
            // Submitting again replaces the request already waiting, which moves its earliest
            // start along. An app opened often would then never reach a refresh at all.
            val alreadyWaiting = pending.orEmpty().any { request: Any? ->
                (request as? BGTaskRequest)?.identifier == ANIME_UPDATE_TASK_IDENTIFIER
            }
            if (!alreadyWaiting) {
                submitUpdateRequest()
            }
        }
    }

    private fun submitUpdateRequest() {
        val request = BGAppRefreshTaskRequest(identifier = ANIME_UPDATE_TASK_IDENTIFIER).apply {
            earliestBeginDate = NSDate().dateByAddingTimeInterval(EARLIEST_REFRESH_DELAY_SECONDS)
        }
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val submitted = BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error.ptr)

            if (!submitted) {
                // Refused when background refresh is off for the app, when the identifier is
                // missing from the Info.plist, or when too many requests are already pending.
                // Either way there is no next pass until something submits one.
                println("$TAG: the next background refresh was refused: ${error.value}")
            }
        }
    }

    private fun runUpdateFor(task: BGTask) {
        // Before any work: a task the system expires, or one whose pass ends badly, would
        // otherwise leave nothing submitted, and background refresh would stop for good.
        schedulePeriodicUpdate()

        refreshPass.runIn(
            object : BackgroundRefreshTask {
                override fun setExpirationHandler(handler: (() -> Unit)?) {
                    task.expirationHandler = handler
                }

                override fun complete(success: Boolean) =
                    task.setTaskCompletedWithSuccess(success = success)
            }
        )
    }

    private companion object {
        private const val EARLIEST_REFRESH_DELAY_SECONDS = 15 * 60.0
    }
}
