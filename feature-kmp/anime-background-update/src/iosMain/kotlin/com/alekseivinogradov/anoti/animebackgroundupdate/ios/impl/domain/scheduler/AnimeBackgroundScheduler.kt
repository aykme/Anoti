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
 * Registers the BGAppRefreshTask handler and schedules the next run. The task identifier
 * above MUST also be listed in the iOS app target's Info.plist under
 * `BGTaskSchedulerPermittedIdentifiers` — that file doesn't exist in this repo yet (no iOS
 * app shell has been created), so this class compiles and is ready to use, but scheduling
 * will silently fail at runtime until an iOS host app registers the identifier. This is a
 * hard external dependency this plan cannot close by itself; tracked here so it isn't
 * forgotten when iOS app work starts.
 *
 * The launch handler below fulfills the `BGTaskScheduler` completion contract: it calls
 * `setTaskCompletedWithSuccess` when the update finishes (or fails) and installs an
 * `expirationHandler` that cancels the in-flight work and reports failure if iOS revokes
 * background time first. `job.cancel()` only requests cooperative cancellation and the
 * `expirationHandler` can run on a different queue than the coroutine's dispatcher, so both
 * paths race to complete the same task; an `AtomicBoolean` guard ensures
 * `setTaskCompletedWithSuccess` is invoked at most once per task invocation, whichever path
 * wins. This can only be exercised end-to-end on a real iOS host app under an actual
 * background execution grant — there is no way to verify it at runtime without one, same
 * limitation as the Info.plist registration gap above.
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
                } catch (exception: Throwable) {
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
