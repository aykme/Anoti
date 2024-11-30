package com.alekseivinogradov.network.impl.data

import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.delay
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

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
            if (callAttempt < maxAttempt) {
                delay(attemptDelay * callAttempt)
                call(
                    apiCall = apiCall,
                    callAttempt = callAttempt + 1
                )
            } else when (throwable) {
                is HttpException -> CallResult.HttpError(throwable.code(), throwable)
                else -> CallResult.OtherError(throwable)
            }
        }
    }
}
