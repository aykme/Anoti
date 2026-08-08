package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.usecase

/**
 * Triggers a one-off background update of the whole saved anime library.
 */
interface UpdateAllAnimeInBackgroundOnceUsecase {
    fun execute()
}
