package com.alekseivinogradov.anoti.animenotification.kmp.impl.presentation.poster

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class LoadWithTimeoutTest {

    @Test
    fun missingUrlSkipsTheLoad() = runTest {
        //Given
        var loadCalled = false
        val load: suspend (String) -> String = {
            loadCalled = true
            POSTER
        }

        //When
        val result = loadWithTimeout(imageUrl = null, timeoutMillis = TIMEOUT_MILLIS, load = load)

        //Then
        assertNull(result)
        assertFalse(loadCalled)
    }

    @Test
    fun loadedPosterIsReturned() = runTest {
        //Given
        val load: suspend (String) -> String = { url: String -> "$POSTER:$url" }

        //When
        val result = loadWithTimeout(imageUrl = URL, timeoutMillis = TIMEOUT_MILLIS, load = load)

        //Then
        assertEquals("$POSTER:$URL", result)
    }

    @Test
    fun stalledLoadTimesOutToNoPoster() = runTest {
        //Given
        val load: suspend (String) -> String = { awaitCancellation() }

        //When
        val result = loadWithTimeout(imageUrl = URL, timeoutMillis = TIMEOUT_MILLIS, load = load)

        //Then
        assertNull(result)
    }

    @Test
    fun throwingLoadYieldsNoPoster() = runTest {
        //Given
        val load: suspend (String) -> String = { error("decoder failed") }

        //When
        val result = loadWithTimeout(imageUrl = URL, timeoutMillis = TIMEOUT_MILLIS, load = load)

        //Then
        assertNull(result)
    }

    @Test
    fun cancellationIsNotSwallowed() = runTest {
        //Given
        val load: suspend (String) -> String = { throw CancellationException("cancelled") }

        //When
        val failure = runCatching {
            loadWithTimeout(imageUrl = URL, timeoutMillis = TIMEOUT_MILLIS, load = load)
        }

        //Then
        assertFailsWith<CancellationException> { failure.getOrThrow() }
    }

    private companion object {
        const val URL = "https://example.com/poster.jpg"
        const val POSTER = "poster"
        const val TIMEOUT_MILLIS = 10_000L
    }
}
