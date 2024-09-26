package com.alekseivinogradov.network.api.data

import com.alekseivinogradov.network.api.domain.model.CallResult

interface SafeApi {
    val maxAttempt: Int
    val attemptDelay: Long

    suspend fun <T> call(
        callAttempt: Int = 1,
        apiCall: suspend () -> T
    ): CallResult<T>
}
