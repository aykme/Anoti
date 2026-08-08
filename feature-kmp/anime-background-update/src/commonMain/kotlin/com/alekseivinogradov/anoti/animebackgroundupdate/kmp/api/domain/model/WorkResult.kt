package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager.AnimeUpdateManager

/**
 * Outcome of an [AnimeUpdateManager] update run.
 */
sealed interface WorkResult {
    /** Every fetched anime was applied to the database. */
    data object Success : WorkResult

    /** At least one fetch failed; the database was updated only for the anime that succeeded. */
    data object Error : WorkResult
}
