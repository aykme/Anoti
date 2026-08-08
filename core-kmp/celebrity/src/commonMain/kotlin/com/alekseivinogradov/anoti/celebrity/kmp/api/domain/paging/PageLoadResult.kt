package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult

/** Outcome of a [Paginator] page load. */
sealed interface PageLoadResult<T> {
    /**
     * @param items the loaded page's items.
     * @param isFirstPage whether this was the first page (vs. a subsequent one).
     */
    data class Success<T>(val items: List<T>, val isFirstPage: Boolean) : PageLoadResult<T>

    /**
     * A connection/HTTP failure ([CallResult.HttpError] or [CallResult.NetworkError]).
     *
     * @param throwable the underlying error.
     * @param isFirstPage whether this was the first page (vs. a subsequent one).
     */
    data class Error<T>(val throwable: Throwable, val isFirstPage: Boolean) : PageLoadResult<T>

    /**
     * Anything else ([CallResult.OtherError], or [loadPage][Paginator] throwing directly).
     *
     * @param throwable the underlying error.
     * @param isFirstPage whether this was the first page (vs. a subsequent one).
     */
    data class UnexpectedError<T>(
        val throwable: Throwable,
        val isFirstPage: Boolean
    ) : PageLoadResult<T>
}
