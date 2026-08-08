package com.alekseivinogradov.anoti.network.kmp.api.data

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/**
 * Wraps a single network call, classifying any failure into [CallResult] instead of letting the
 * exception propagate, and transparently retrying failures that are likely to succeed on a
 * later attempt (server errors, connectivity/timeout issues).
 *
 * [kotlinx.coroutines.CancellationException] is never caught or retried — it always propagates,
 * so structured concurrency (coroutine cancellation) keeps working normally.
 */
interface SafeApi {
    /**
     * Runs [apiCall] and returns its outcome as a [CallResult].
     *
     * On a retryable failure (5xx [CallResult.HttpError] or any [CallResult.NetworkError]),
     * [apiCall] is retried with a linearly increasing delay between attempts, up to the
     * implementation's configured attempt limit. 4xx [CallResult.HttpError] and
     * [CallResult.OtherError] are never retried, since retrying them can't change the outcome.
     *
     * @param callAttempt the number of the attempt currently being made; callers should not pass
     * this explicitly — it is advanced internally on each retry.
     * @param apiCall the suspending network call to execute.
     */
    suspend fun <T> call(
        callAttempt: Int = 1,
        apiCall: suspend () -> T
    ): CallResult<T>
}
