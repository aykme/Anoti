package com.alekseivinogradov.network.data.remote

import com.alekseivinogradov.network.domain.model.CallResult

interface SafeApi {
    suspend fun <T> call(apiCall: suspend () -> T): CallResult<T>
}