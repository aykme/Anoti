package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.manager

import com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model.WorkResult

interface AnimeUpdateManager {

    companion object {
        const val DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES = 60L
    }

    suspend fun update(): WorkResult
}
