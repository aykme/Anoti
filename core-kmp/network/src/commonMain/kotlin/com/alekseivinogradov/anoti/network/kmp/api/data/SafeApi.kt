package com.alekseivinogradov.anoti.network.kmp.api.data

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/**
 * Safe API calls with retries.
 */
interface SafeApi {
    /**
     * Runs [apiCall] and returns its outcome as [CallResult]. Retries retryable failures
     * (4xx/5xx HTTP errors, network errors) with an increasing delay between attempts.
     *
     * @param callAttempt current attempt number; managed internally, don't pass it explicitly.
     * @param apiCall the network call to run.
     */
    suspend fun <T> call(
        callAttempt: Int = 1,
        apiCall: suspend () -> T
    ): CallResult<T>
}
