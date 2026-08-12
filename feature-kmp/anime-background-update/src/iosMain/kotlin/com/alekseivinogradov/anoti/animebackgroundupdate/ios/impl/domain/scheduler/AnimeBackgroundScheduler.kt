package com.alekseivinogradov.anoti.animebackgroundupdate.ios.impl.domain.scheduler

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler.AnimeBackgroundScheduler
import kotlinx.cinterop.ExperimentalForeignApi
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
 */
@OptIn(ExperimentalForeignApi::class)
class AnimeBackgroundSchedulerImpl(
    private val animeUpdateManager: AnimeUpdateManager,
    private val coroutineScope: CoroutineScope
) : AnimeBackgroundScheduler {

    fun registerTaskHandler() {
        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier = ANIME_UPDATE_TASK_IDENTIFIER,
            usingQueue = null
        ) { task ->
            coroutineScope.launch {
                animeUpdateManager.update()
                schedulePeriodicUpdate()
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
