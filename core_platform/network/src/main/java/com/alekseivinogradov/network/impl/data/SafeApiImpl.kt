package com.alekseivinogradov.network.impl.data

import com.alekseivinogradov.network.api.data.remote.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.delay
import retrofit2.HttpException

object SafeApiImpl : SafeApi {
    override val maxAttempt: Int = 3
    override val attemptDelay: Long = 2500L

    override suspend fun <T> call(
        callAttempt: Int,
        apiCall: suspend () -> T
    ): CallResult<T> {
        return try {
            CallResult.Success(apiCall.invoke())
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