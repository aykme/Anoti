package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.CancellationException

/**
 * Pages through [loadPage] results one page at a time, tracking the next page number and
 * whether the end of the list has been reached.
 *
 * @param firstPage page number to start from.
 * @param loadPage loads one page of items; wraps failures in [CallResult].
 */
class Paginator<T>(
    private val firstPage: Int,
    private val loadPage: suspend (page: Int) -> CallResult<List<T>>
) {
    private var nextPage: Int = firstPage
    private var endReached: Boolean = false
    private var isLoading: Boolean = false

    /** Resets to [firstPage] and loads it. */
    suspend fun loadFirstPage(): PageLoadResult<T> {
        nextPage = firstPage
        endReached = false
        return load(page = firstPage, isFirstPage = true)
    }

    /**
     * Loads the next page, or returns null if the end was reached or a load is already in
     * flight.
     */
    suspend fun loadNextPage(): PageLoadResult<T>? {
        if (endReached || isLoading) return null
        return load(page = nextPage, isFirstPage = false)
    }

    private suspend fun load(page: Int, isFirstPage: Boolean): PageLoadResult<T> {
        isLoading = true
        val outcome = try {
            loadPage(page)
        } catch (cancellation: CancellationException) {
            isLoading = false
            throw cancellation
        } catch (throwable: Throwable) {
            isLoading = false
            return PageLoadResult.UnexpectedError(throwable = throwable, isFirstPage = isFirstPage)
        }
        isLoading = false
        return when (outcome) {
            is CallResult.Success -> {
                if (outcome.value.isEmpty()) {
                    endReached = true
                } else {
                    nextPage = page + 1
                }
                PageLoadResult.Success(items = outcome.value, isFirstPage = isFirstPage)
            }

            is CallResult.HttpError,
            is CallResult.NetworkError -> PageLoadResult.Error(
                throwable = outcome.throwable,
                isFirstPage = isFirstPage
            )

            is CallResult.OtherError -> PageLoadResult.UnexpectedError(
                throwable = outcome.throwable,
                isFirstPage = isFirstPage
            )
        }
    }
}
