package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.fake

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.BackgroundRefreshTask

/**
 * Stands in for the platform background task, recording every outcome it is told about.
 */
internal class BackgroundRefreshTaskFake : BackgroundRefreshTask {

    override var expirationHandler: (() -> Unit)? = null

    /** Every outcome the task was told about, oldest first. */
    val outcomes: List<Boolean>
        field = mutableListOf<Boolean>()

    override fun complete(success: Boolean) {
        outcomes += success
    }

    /** Takes the task back the way the platform would. */
    fun expire() {
        expirationHandler?.invoke()
    }
}
