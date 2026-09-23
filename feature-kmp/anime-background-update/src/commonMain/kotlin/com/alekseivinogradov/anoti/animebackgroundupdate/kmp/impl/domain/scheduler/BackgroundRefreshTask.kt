package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler

/**
 * The platform background task one update pass runs inside.
 */
internal interface BackgroundRefreshTask {

    /**
     * Installs what the platform runs when it takes the task back before the pass is over, or
     * drops the one already installed.
     *
     * @param handler what to run, or null to leave the task with none.
     */
    fun setExpirationHandler(handler: (() -> Unit)?)

    /** Tells the platform the pass is over. */
    fun complete(success: Boolean)
}
