package com.alekseivinogradov.anoti.network.kmp.api.data

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/**
 * Safe API calls with retries.
 */
interface SafeApi {
    /**
     * Runs [apiCall] and returns its outcome as [CallResult]. Retries any failure except
     * cancellation, with an increasing delay between attempts, up to a fixed attempt count.
     *
     * @param apiCall the network call to run.
     */
    suspend fun <T> call(apiCall: suspend () -> T): CallResult<T>
}
