package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler

/**
 * The platform background task one update pass runs inside.
 */
internal interface BackgroundRefreshTask {

    /** What the platform runs when it takes the task back before the pass is over. */
    var expirationHandler: (() -> Unit)?

    /** Tells the platform the pass is over. */
    fun complete(success: Boolean)
}
