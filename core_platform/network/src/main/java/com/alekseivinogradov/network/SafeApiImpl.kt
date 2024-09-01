package com.alekseivinogradov.network

import com.alekseivinogradov.network.domain.CallResult
import com.alekseivinogradov.network.domain.SafeApi
import retrofit2.HttpException

class SafeApiImpl : SafeApi {
    override suspend fun <T> call(apiCall: suspend () -> T): CallResult<T> {
        return try {
            CallResult.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> CallResult.HttpError(throwable.code(), throwable)
                else -> CallResult.OtherError(throwable)
            }
        }
    }
}