package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler

/**
 * Schedules periodic background updates of the whole saved anime library on the host platform.
 */
interface AnimeBackgroundScheduler {

    /** Schedules (or re-schedules) the next periodic update pass. */
    fun schedulePeriodicUpdate()
}
