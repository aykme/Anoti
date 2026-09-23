package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.fake

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler.BackgroundRefreshTask

/**
 * Stands in for the platform background task, recording everything it is told.
 */
internal class BackgroundRefreshTaskFake : BackgroundRefreshTask {

    /** Every outcome the task was told about, oldest first. */
    val outcomes: List<Boolean>
        field = mutableListOf<Boolean>()

    /**
     * Whether the task could be taken back after each hand-over, oldest first. A pass that
     * installs a handler and then drops it leaves `[true, false]` here.
     */
    val takeBackOffers: List<Boolean>
        field = mutableListOf<Boolean>()

    private var expirationHandler: (() -> Unit)? = null

    override fun setExpirationHandler(handler: (() -> Unit)?) {
        expirationHandler = handler
        takeBackOffers += handler != null
    }

    override fun complete(success: Boolean) {
        outcomes += success
    }

    /** Takes the task back the way the platform would. */
    fun expire() {
        expirationHandler?.invoke()
    }
}
