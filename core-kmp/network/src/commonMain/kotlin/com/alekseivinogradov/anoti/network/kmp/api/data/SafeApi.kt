package com.alekseivinogradov.anoti.network.kmp.api.data

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

interface SafeApi {
    suspend fun <T> call(
        callAttempt: Int = 1,
        apiCall: suspend () -> T
    ): CallResult<T>
}
