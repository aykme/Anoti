package com.alekseivinogradov.anoti.network.kmp.impl.data.fake

import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlin.coroutines.cancellation.CancellationException

/**
 * Test/preview [SafeApi]: never retries, classifies any failure as [CallResult.OtherError].
 */
class SafeApiFake : SafeApi {
    override suspend fun <T> call(
        callAttempt: Int,
        apiCall: suspend () -> T
    ): CallResult<T> {
        return try {
            CallResult.Success(apiCall.invoke())
        } catch (e: CancellationException) {
            throw e
        } catch (
            @Suppress("TooGenericExceptionCaught") throwable: Throwable
        ) {
            // Catching everything and wrapping it as a CallResult is this class's whole purpose.
            CallResult.OtherError(throwable)
        }
    }
}
