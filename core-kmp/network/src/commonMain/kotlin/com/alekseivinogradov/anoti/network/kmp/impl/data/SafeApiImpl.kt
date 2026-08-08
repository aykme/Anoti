package com.alekseivinogradov.anoti.network.kmp.impl.data

import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.io.IOException

/**
 * Ktor-based [SafeApi]. Classifies [kotlinx.io.IOException] (connection failures, all of Ktor's
 * timeout exceptions) as [CallResult.NetworkError], and any [ResponseException] as
 * [CallResult.HttpError] using its actual HTTP status code. Everything else becomes
 * [CallResult.OtherError].
 *
 * @param maxAttempt total number of attempts before giving up on a retryable failure (1 = never
 * retry).
 * @param attemptDelay base delay between retries; the actual wait grows linearly with the attempt
 * number (`attemptDelay * callAttempt`).
 */
class SafeApiImpl(
    private val maxAttempt: Int,
    private val attemptDelay: Duration
) : SafeApi {
    override suspend fun <T> call(
        callAttempt: Int,
        apiCall: suspend () -> T
    ): CallResult<T> {
        return try {
            CallResult.Success(apiCall.invoke())
        } catch (e: CancellationException) {
            throw e
        } catch (throwable: Throwable) {
            if (isRetryable(throwable) && callAttempt < maxAttempt) {
                delay(attemptDelay * callAttempt)
                call(
                    callAttempt = callAttempt + 1,
                    apiCall = apiCall
                )
            } else {
                classify(throwable)
            }
        }
    }

    private fun isRetryable(throwable: Throwable): Boolean = when (throwable) {
        is ResponseException -> throwable.response.status.value >= 500
        is IOException -> true
        else -> false
    }

    private fun classify(throwable: Throwable): CallResult<Nothing> = when (throwable) {
        is ResponseException -> CallResult.HttpError(
            code = throwable.response.status.value,
            throwable = throwable
        )

        is IOException -> CallResult.NetworkError(throwable)
        else -> CallResult.OtherError(throwable)
    }
}
