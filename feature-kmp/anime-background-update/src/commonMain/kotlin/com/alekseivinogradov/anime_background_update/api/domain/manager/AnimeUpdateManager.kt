package com.alekseivinogradov.anime_background_update.api.domain.manager

import com.alekseivinogradov.anime_background_update.api.domain.model.WorkResult

interface AnimeUpdateManager {

    suspend fun update(): WorkResult
}
