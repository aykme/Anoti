package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.scheduler

/**
 * Schedules periodic background updates of the whole saved anime library on the host platform.
 */
interface AnimeBackgroundScheduler {

    /** Makes sure a periodic update pass is scheduled, without pushing back one already due. */
    fun schedulePeriodicUpdate()
}
