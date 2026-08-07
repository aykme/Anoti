package com.alekseivinogradov.celebrity.api.domain.paging

import com.alekseivinogradov.network.api.domain.model.CallResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

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
        val error = Throwable("boom")
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                if (page == 2 && shouldFail) {
                    CallResult.OtherError(error)
                } else {
                    CallResult.Success(listOf("item$page"))
                }
            }
        )
        paginator.loadFirstPage()
        val firstAttemptResult = paginator.loadNextPage() // page 2 fails, does not advance
        assertEquals(
            PageLoadResult.Error(throwable = error, isFirstPage = false),
            firstAttemptResult
        )

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
        var firstPageIsEmpty = true
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                if (page == 1 && firstPageIsEmpty) {
                    CallResult.Success(emptyList())
                } else {
                    CallResult.Success(listOf("item$page"))
                }
            }
        )
        paginator.loadFirstPage() // page 1 -> empty -> endReached = true
        assertNull(paginator.loadNextPage()) // blocked by endReached

        firstPageIsEmpty = false
        paginator.loadFirstPage() // reset: page 1 now has data
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

    @Test
    fun loadFirstPageDoesNotSwallowCancellation() = runTest {
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { throw CancellationException("cancelled") }
        )

        assertFailsWith<CancellationException> {
            paginator.loadFirstPage()
        }
    }
}
