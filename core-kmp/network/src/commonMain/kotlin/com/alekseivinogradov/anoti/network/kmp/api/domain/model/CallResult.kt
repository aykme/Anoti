package com.alekseivinogradov.anoti.network.kmp.api.domain.model

/**
 * The outcome of a network call made through
 * [SafeApi][com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi].
 */
sealed class CallResult<out T> {
    data class Success<out T>(val value: T) : CallResult<T>()

    /**
     * Any failed outcome. Match on [Failure] instead of the individual subtypes when the caller
     * only needs to know "did it fail", not why.
     */
    sealed class Failure : CallResult<Nothing>() {
        abstract val throwable: Throwable
    }

    /**
     * The server responded with a non-2xx status. [code] is the HTTP status code when available.
     */
    data class HttpError(val code: Int? = null, override val throwable: Throwable) : Failure()

    /**
     * The request never got a response: connection failure, timeout, DNS failure, etc.
     */
    data class NetworkError(override val throwable: Throwable) : Failure()

    /**
     * Anything else — e.g. a response body that failed to deserialize, or an unexpected
     * exception thrown by the call itself. Not a connectivity problem, so callers should not
     * label it as one in user-facing messaging.
     */
    data class OtherError(override val throwable: Throwable) : Failure()
}
