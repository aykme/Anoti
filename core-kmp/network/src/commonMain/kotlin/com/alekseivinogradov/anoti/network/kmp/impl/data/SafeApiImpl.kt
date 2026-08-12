package com.alekseivinogradov.anoti.network.kmp.impl.data

import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

/**
 * Ktor-based [SafeApi]. [maxAttempt] is the total number of attempts (1 = never retry);
 * [attemptDelay] is the base delay between retries, growing linearly with the attempt number.
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
        } catch (
            // Catching everything and classifying it into a CallResult is this class's whole purpose.
            @Suppress("TooGenericExceptionCaught") throwable: Throwable
        ) {
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
        is ResponseException -> throwable.response.status.value >= HTTP_SERVER_ERROR_STATUS
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

    private companion object {
        private const val HTTP_SERVER_ERROR_STATUS = 500
    }
}
