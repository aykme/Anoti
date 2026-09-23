package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.usecase.fake

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase.UpdateAllAnimeInBackgroundOnceUsecase

/**
 * Schedules nothing and counts the requests instead, so a test can assert that the code under
 * test asked for a background update without one actually running.
 */
class UpdateAllAnimeInBackgroundOnceUsecaseFake : UpdateAllAnimeInBackgroundOnceUsecase {

    /** How many times an update was asked for. */
    var executeCount = 0
        private set

    override fun execute() {
        executeCount++
    }
}
