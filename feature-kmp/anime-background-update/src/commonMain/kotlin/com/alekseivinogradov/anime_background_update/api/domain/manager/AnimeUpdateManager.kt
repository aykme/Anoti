package com.alekseivinogradov.anime_background_update.api.domain.manager

import com.alekseivinogradov.anime_background_update.api.domain.model.WorkResult
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface AnimeUpdateManager {

    companion object {
        const val defaultAnimeUpdateWorkIntervalMinutes = 60L
    }

    suspend fun update(): WorkResult
}
