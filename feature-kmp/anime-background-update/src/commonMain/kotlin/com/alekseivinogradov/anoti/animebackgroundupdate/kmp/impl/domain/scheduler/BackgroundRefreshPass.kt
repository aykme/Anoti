package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * Runs one update pass inside a platform background task and reports how it ended.
 *
 * Every way the pass can end reports an outcome. A task left uncompleted costs the app its
 * future background passes.
 *
 * @param animeUpdateManager runs the pass.
 * @param coroutineScope scope the pass runs in.
 */
internal class BackgroundRefreshPass(
    private val animeUpdateManager: AnimeUpdateManager,
    private val coroutineScope: CoroutineScope
) {

    /** Starts the pass for [task]. */
    fun runIn(task: BackgroundRefreshTask) {
        val completion = OneShotCompletion { success: Boolean ->
            task.complete(success)
            // The handler holds this completion and this completion holds the task, so leaving
            // it in place keeps every finished task alive for as long as the process runs.
            task.setExpirationHandler(null)
        }

        val job = coroutineScope.launch(start = CoroutineStart.LAZY) {
            completion.complete(animeUpdateManager.update() == WorkResult.Success)
        }
        // Installed before the completion handler below, which is what drops it again: on a
        // scope that is already gone, that handler runs the moment it is registered, and a
        // handler installed after it would be left on a task the platform has taken back.
        task.setExpirationHandler { job.cancel() }
        // Whatever ends the pass — a throw, the scope being canceled, the platform taking the
        // task back — the platform is told.
        job.invokeOnCompletion { completion.complete(success = false) }

        job.start()
    }
}
