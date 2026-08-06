package com.alekseivinogradov.celebrity.api.domain.paging

import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.CancellationException

class Paginator<T>(
    private val firstPage: Int,
    private val loadPage: suspend (page: Int) -> CallResult<List<T>>
) {
    private var nextPage: Int = firstPage
    private var endReached: Boolean = false
    private var isLoading: Boolean = false

    suspend fun loadFirstPage(): PageLoadResult<T> {
        nextPage = firstPage
        endReached = false
        return load(page = firstPage, isFirstPage = true)
    }

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

            is CallResult.HttpError -> PageLoadResult.Error(
                throwable = outcome.throwable,
                isFirstPage = isFirstPage
            )

            is CallResult.OtherError -> PageLoadResult.Error(
                throwable = outcome.throwable,
                isFirstPage = isFirstPage
            )
        }
    }
}

sealed interface PageLoadResult<T> {
    data class Success<T>(val items: List<T>, val isFirstPage: Boolean) : PageLoadResult<T>
    data class Error<T>(val throwable: Throwable, val isFirstPage: Boolean) : PageLoadResult<T>
    data class UnexpectedError<T>(val throwable: Throwable, val isFirstPage: Boolean) : PageLoadResult<T>
}
