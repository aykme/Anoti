package com.alekseivinogradov.anime_background_update.api.domain.model

sealed interface WorkResult {
    data object Success : WorkResult
    data object Error : WorkResult
}
