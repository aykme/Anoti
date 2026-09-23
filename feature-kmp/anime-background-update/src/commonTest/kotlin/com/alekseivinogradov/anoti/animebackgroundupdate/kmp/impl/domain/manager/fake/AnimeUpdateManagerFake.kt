package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.manager.fake

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager
import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult

/**
 * Updates nothing and answers with [result], counting the passes it was asked for.
 *
 * @param result the outcome every pass reports.
 * @param onUpdate runs before the pass is counted. Suspend in it to hold a pass open, or throw
 * from it to stand in for one that fails outright.
 */
class AnimeUpdateManagerFake(
    private val result: WorkResult = WorkResult.Success,
    private val onUpdate: suspend () -> Unit = {}
) : AnimeUpdateManager {

    /** How many passes were asked for. */
    var updateCount = 0
        private set

    override suspend fun update(): WorkResult {
        onUpdate()
        updateCount++
        return result
    }
}
