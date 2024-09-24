package com.alekseivinogradov.network.api.domain.model

sealed class CallResult<out T> {
    data class Success<out T>(val value: T) : CallResult<T>()
    data class HttpError(val code: Int? = null, val throwable: Throwable) : CallResult<Nothing>()
    data class OtherError(val throwable: Throwable) : CallResult<Nothing>()
}
