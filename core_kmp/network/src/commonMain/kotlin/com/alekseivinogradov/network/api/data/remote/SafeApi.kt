package com.alekseivinogradov.network.api.data.remote

import com.alekseivinogradov.network.api.domain.model.CallResult

interface SafeApi {
    suspend fun <T> call(apiCall: suspend () -> T): CallResult<T>
}