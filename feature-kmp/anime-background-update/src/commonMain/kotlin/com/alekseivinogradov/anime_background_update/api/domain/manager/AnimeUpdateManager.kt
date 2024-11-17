package com.alekseivinogradov.anime_background_update.api.domain.manager

import com.alekseivinogradov.anime_background_update.api.domain.model.WorkResult

interface AnimeUpdateManager {

    companion object {
        const val DEFAULT_ANIME_UPDATE_INTERVAL_MINUTES = 60L
    }

    suspend fun update(): WorkResult
}
