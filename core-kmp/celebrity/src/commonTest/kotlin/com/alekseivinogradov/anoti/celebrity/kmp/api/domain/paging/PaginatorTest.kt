package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.paging

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PaginatorTest {

    @Test
    fun loadFirstPageReturnsItemsFromFirstPage() = runTest {
        //Given
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page -> CallResult.Success(listOf("page$page-item1", "page$page-item2")) }
        )

        //When
        val result = paginator.loadFirstPage()

        //Then
        assertEquals(
            PageLoadResult.Success(
                items = listOf("page1-item1", "page1-item2"),
                isFirstPage = true
            ),
            result
        )
    }

    @Test
    fun loadNextPageRequestsTheFollowingPageNumber() = runTest {
        //Given
        val requestedPages = mutableListOf<Int>()
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                CallResult.Success(listOf("item"))
            }
        )
        paginator.loadFirstPage()

        //When
        paginator.loadNextPage()

        //Then
        assertEquals(listOf(1, 2), requestedPages)
    }

    @Test
    fun loadNextPageReturnsNullAfterEmptyPageMarksEndReached() = runTest {
        //Given
        var callCount = 0
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                callCount++
                if (page == 1) {
                    CallResult.Success(emptyList())
                } else {
                    CallResult.Success(listOf("unreachable"))
                }
            }
        )
        paginator.loadFirstPage()

        //When
        val result = paginator.loadNextPage()

        //Then
        assertNull(result)
        assertEquals(1, callCount)
    }

    @Test
    fun loadNextPageDoesNotAdvancePageOnError() = runTest {
        //Given
        val requestedPages = mutableListOf<Int>()
        var shouldFail = true
        val error = Throwable("boom")
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                requestedPages.add(page)
                if (page == 2 && shouldFail) {
                    CallResult.NetworkError(error)
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

        //When
        shouldFail = false
        val retryResult = paginator.loadNextPage() // retries page 2

        //Then
        assertEquals(listOf(1, 2, 2), requestedPages)
        assertEquals(
            PageLoadResult.Success(items = listOf("item2"), isFirstPage = false),
            retryResult
        )
    }

    @Test
    fun loadFirstPageResetsEndReachedAndPageCounter() = runTest {
        //Given
        val requestedPages = mutableListOf<Int>()
        var firstPageIsEmpty = true
        val paginator = Paginator(
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

        //When
        firstPageIsEmpty = false
        paginator.loadFirstPage() // reset: page 1 now has data
        requestedPages.clear()
        paginator.loadNextPage()

        //Then
        assertEquals(listOf(2), requestedPages)
    }

    @Test
    fun loadNextPageIgnoresConcurrentCallWhileAlreadyLoading() = runTest {
        //Given
        val callStarted = CompletableDeferred<Unit>()
        val releaseFirstCall = CompletableDeferred<Unit>()
        var callCount = 0
        val paginator = Paginator(
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

        //When
        val firstCall = launch { paginator.loadNextPage() }
        callStarted.await()
        val secondCallResult = paginator.loadNextPage()
        releaseFirstCall.complete(Unit)
        firstCall.join()

        //Then
        assertNull(secondCallResult)
        assertEquals(2, callCount)
    }

    @Test
    fun firstPageErrorIsReportedAsFirstPageError() = runTest {
        //Given
        val error = Throwable("network down")
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { CallResult.HttpError(code = 500, throwable = error) }
        )

        //When
        val result = paginator.loadFirstPage()

        //Then
        assertEquals(PageLoadResult.Error(throwable = error, isFirstPage = true), result)
    }

    @Test
    fun firstPageOtherErrorIsReportedAsUnexpectedErrorNotAsConnectionError() = runTest {
        //Given
        val error = IllegalStateException("unexpected")
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { CallResult.OtherError(error) }
        )

        //When
        val result = paginator.loadFirstPage()

        //Then
        assertEquals(PageLoadResult.UnexpectedError(throwable = error, isFirstPage = true), result)
    }

    @Test
    fun loadFirstPageReturnsUnexpectedErrorWhenLoadPageThrows() = runTest {
        //Given
        val error = IllegalStateException("boom")
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { throw error }
        )

        //When
        val result = paginator.loadFirstPage()

        //Then
        assertEquals(PageLoadResult.UnexpectedError(throwable = error, isFirstPage = true), result)
    }

    @Test
    fun loadFirstPageDoesNotSwallowCancellation() = runTest {
        //Given
        val paginator = Paginator<String>(
            firstPage = 1,
            loadPage = { throw CancellationException("cancelled") }
        )

        //When / Then
        assertFailsWith<CancellationException> {
            paginator.loadFirstPage()
        }
    }

    @Test
    fun aLoadThatThrewLeavesTheNextOneFreeToRun() = runTest {
        //Given
        var shouldThrow = true
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                if (shouldThrow) error("boom")
                CallResult.Success(listOf("item$page"))
            }
        )
        paginator.loadFirstPage()

        //When
        shouldThrow = false
        val result = paginator.loadNextPage()

        //Then
        assertEquals(
            PageLoadResult.Success(items = listOf("item1"), isFirstPage = false),
            result
        )
    }

    @Test
    fun aCancelledLoadLeavesTheNextOneFreeToRun() = runTest {
        //Given
        var shouldCancel = true
        val paginator = Paginator(
            firstPage = 1,
            loadPage = { page ->
                if (shouldCancel) throw CancellationException("cancelled")
                CallResult.Success(listOf("item$page"))
            }
        )
        assertFailsWith<CancellationException> { paginator.loadFirstPage() }

        //When
        shouldCancel = false
        val result = paginator.loadNextPage()

        //Then
        assertEquals(
            PageLoadResult.Success(items = listOf("item1"), isFirstPage = false),
            result
        )
    }
}
