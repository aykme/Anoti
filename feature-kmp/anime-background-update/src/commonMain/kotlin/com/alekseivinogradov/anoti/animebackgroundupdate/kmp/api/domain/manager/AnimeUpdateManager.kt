package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult

/**
 * Fetches fresh data for every anime saved in the database and updates it, notifying about
 * newly aired episodes along the way.
 */
interface AnimeUpdateManager {

    companion object {
        const val DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES = 60L
    }

    /** Runs one update pass over the whole saved anime library. */
    suspend fun update(): WorkResult
}
