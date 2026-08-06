package com.alekseivinogradov.celebrity.api.domain.paging

import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaginatorTest {

    @Test
    fun loadFirstPageReturnsItemsFromFirstPage() = runTest {
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page -> CallResult.Success(listOf("page$page-item1", "page$page-item2")) }
        )

        val result = paginator.loadFirstPage()

        assertEquals(
            PageLoadResult.Success(items = listOf("page1-item1", "page1-item2"), isFirstPage = true),
            result
        )
    }

    @Test
    fun loadNextPageRequestsTheFollowingPageNumber() = runTest {
        val requestedPages = mutableListOf<Int>()
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                CallResult.Success(listOf("item"))
            }
        )
        paginator.loadFirstPage()

        paginator.loadNextPage()

        assertEquals(listOf(1, 2), requestedPages)
    }

    @Test
    fun loadNextPageReturnsNullAfterEmptyPageMarksEndReached() = runTest {
        var callCount = 0
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { page ->
                callCount++
                if (page == 1) CallResult.Success(emptyList()) else CallResult.Success(listOf("unreachable"))
            }
        )
        paginator.loadFirstPage()

        val result = paginator.loadNextPage()

        assertNull(result)
        assertEquals(1, callCount)
    }

    @Test
    fun loadNextPageDoesNotAdvancePageOnError() = runTest {
        val requestedPages = mutableListOf<Int>()
        var shouldFail = true
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                if (page == 2 && shouldFail) {
                    CallResult.OtherError(Throwable("boom"))
                } else {
                    CallResult.Success(listOf("item$page"))
                }
            }
        )
        paginator.loadFirstPage()
        paginator.loadNextPage() // page 2 fails, does not advance

        shouldFail = false
        val retryResult = paginator.loadNextPage() // retries page 2

        assertEquals(listOf(1, 2, 2), requestedPages)
        assertEquals(
            PageLoadResult.Success(items = listOf("item2"), isFirstPage = false),
            retryResult
        )
    }

    @Test
    fun loadFirstPageResetsEndReachedAndPageCounter() = runTest {
        val requestedPages = mutableListOf<Int>()
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                if (page == 1) CallResult.Success(emptyList()) else CallResult.Success(listOf("item"))
            }
        )
        paginator.loadFirstPage() // page 1 -> empty -> endReached = true
        assertNull(paginator.loadNextPage()) // blocked by endReached

        paginator.loadFirstPage() // reset
        requestedPages.clear()
        paginator.loadNextPage()

        assertEquals(listOf(2), requestedPages)
    }

    @Test
    fun loadNextPageIgnoresConcurrentCallWhileAlreadyLoading() = runTest {
        val callStarted = CompletableDeferred<Unit>()
        val releaseFirstCall = CompletableDeferred<Unit>()
        var callCount = 0
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { page ->
                callCount++
                if (page == 2) {
                    callStarted.complete(Unit)
                    releaseFirstCall.await()
                }
                CallResult.Success(listOf("item$page"))
            }
        )
        paginator.loadFirstPage()

        val firstCall = launch { paginator.loadNextPage() }
        callStarted.await()
        val secondCallResult = paginator.loadNextPage()
        releaseFirstCall.complete(Unit)
        firstCall.join()

        assertNull(secondCallResult)
        assertEquals(2, callCount)
    }

    @Test
    fun firstPageErrorIsReportedAsFirstPageError() = runTest {
        val error = Throwable("network down")
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { CallResult.HttpError(code = 500, throwable = error) }
        )

        val result = paginator.loadFirstPage()

        assertEquals(PageLoadResult.Error(throwable = error, isFirstPage = true), result)
    }

    @Test
    fun loadFirstPageReturnsUnexpectedErrorWhenLoadPageThrows() = runTest {
        val error = IllegalStateException("boom")
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { throw error }
        )

        val result = paginator.loadFirstPage()

        assertEquals(PageLoadResult.UnexpectedError(throwable = error, isFirstPage = true), result)
    }
}
