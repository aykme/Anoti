package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.api.domain.model

sealed interface WorkResult {
    data object Success : WorkResult
    data object Error : WorkResult
}
