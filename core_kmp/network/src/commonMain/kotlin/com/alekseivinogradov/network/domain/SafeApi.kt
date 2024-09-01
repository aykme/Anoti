package com.alekseivinogradov.network.domain

interface SafeApi {
    suspend fun <T> call(apiCall: suspend () -> T): CallResult<T>
}