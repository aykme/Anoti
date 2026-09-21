package com.alekseivinogradov.anoti.network.kmp.impl.data.fake

import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SafeApiFakeTest {

    @Test
    fun callReturnsTheValueTheApiCallProduced() = runTest {
        //Given
        val safeApi = SafeApiFake()

        //When
        val result = safeApi.call { "ok" }

        //Then
        assertEquals(CallResult.Success("ok"), result)
    }

    @Test
    fun callRunsTheApiCallExactlyOnceWhenItFails() = runTest {
        //Given
        val safeApi = SafeApiFake()
        var attempts = 0

        //When
        val result = safeApi.call {
            attempts++
            error("boom")
        }

        //Then
        assertIs<CallResult.OtherError>(result)
        assertEquals(1, attempts, "the fake must not retry")
    }

    @Test
    fun callRethrowsCancellationWithoutWrappingIt() = runTest {
        //Given
        val safeApi = SafeApiFake()

        //When / Then
        assertFailsWith<CancellationException> {
            safeApi.call { throw CancellationException("cancelled") }
        }
    }
}
