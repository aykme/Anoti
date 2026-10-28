package com.alekseivinogradov.anoti.webpageopener.ios.impl.domain.usecase

import kotlinx.coroutines.test.runTest
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenWebPageUsecaseImplTest {

    private val received = mutableListOf<NSURL>()

    private fun usecaseAnswering(opened: Boolean) = OpenWebPageUsecaseImpl(
        openUrl = { url: NSURL, onResult: (Boolean) -> Unit ->
            received += url
            onResult(opened)
        }
    )

    @Test
    fun anAddressThatDoesNotParseIsRefusedWithoutAskingTheSystem() = runTest {
        //Given
        val usecase = usecaseAnswering(opened = true)

        //When
        val opened = usecase.execute(UNPARSABLE_URL)

        //Then
        assertFalse(opened)
        assertTrue(received.isEmpty())
    }

    @Test
    fun aPageTheSystemOpensAnswersTrueAndReachesItParsed() = runTest {
        //Given
        val usecase = usecaseAnswering(opened = true)

        //When
        val opened = usecase.execute(PAGE_URL)

        //Then
        assertTrue(opened)
        assertEquals(PAGE_URL, received.single().absoluteString)
    }

    @Test
    fun aPageNoAppTakesAnswersFalse() = runTest {
        //Given
        val usecase = usecaseAnswering(opened = false)

        //When
        val opened = usecase.execute(PAGE_URL)

        //Then
        assertFalse(opened)
    }
}

private const val PAGE_URL = "https://example.com/page/"

// NSURL refuses the empty string; iOS 17 and later percent-encode most other bad input.
private const val UNPARSABLE_URL = ""
