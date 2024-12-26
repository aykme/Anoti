package com.alekseivinogradov.anime_favorites.impl.data.source

import com.alekseivinogradov.network.api.data.SafeApi
import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlin.coroutines.cancellation.CancellationException

class SafeApiFake() : SafeApi {
    override suspend fun <T> call(
        callAttempt: Int, apiCall: suspend () -> T
    ): CallResult<T> {
        return try {
            CallResult.Success(apiCall.invoke())
        } catch (e: CancellationException) {
            throw e
        } catch (throwable: Throwable) {
            CallResult.OtherError(throwable)
        }
    }
}
