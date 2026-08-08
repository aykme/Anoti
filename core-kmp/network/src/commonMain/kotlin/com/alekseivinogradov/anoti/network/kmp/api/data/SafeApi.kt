package com.alekseivinogradov.anoti.network.kmp.api.data

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/**
 * Runs a network call and returns its outcome as [CallResult] instead of throwing.
 */
interface SafeApi {
    /**
     * Runs [apiCall], retrying retryable failures (5xx, network errors) with increasing delay.
     * [callAttempt] is managed internally — callers should not pass it.
     */
    suspend fun <T> call(
        callAttempt: Int = 1,
        apiCall: suspend () -> T
    ): CallResult<T>
}
