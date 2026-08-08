package com.alekseivinogradov.anoti.network.kmp.impl.data

import com.alekseivinogradov.anoti.network.kmp.api.data.SafeApi
import com.alekseivinogradov.anoti.network.kmp.api.domain.model.CallResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class SafeApiImplTest {

    @Test
    fun callReturnsSuccessWithoutRetryingOnFirstTry() = runTest {
        //Given
        val safeApi = createSafeApi()
        var attempts = 0

        //When
        val result = safeApi.call {
            attempts++
            "ok"
        }

        //Then
        assertEquals(CallResult.Success("ok"), result)
        assertEquals(1, attempts)
    }

    @Test
    fun callRethrowsCancellationWithoutWrappingIt() = runTest {
        //Given
        val safeApi = createSafeApi()

        //When / Then
        assertFailsWith<CancellationException> {
            safeApi.call { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun callReturnsHttpErrorWithoutRetryOnClientError() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3)
        var attempts = 0
        val client = mockClient(MockEngine {
            attempts++
            respond(content = ByteReadChannel(""), status = HttpStatusCode.BadRequest)
        })

        //When
        val result = safeApi.call { client.get("https://test/") }

        //Then
        assertIs<CallResult.HttpError>(result)
        assertEquals(400, result.code)
        assertEquals(1, attempts)
    }

    @Test
    fun callRetriesServerErrorAndReturnsHttpErrorAfterExhaustingAttempts() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3)
        var attempts = 0
        val client = mockClient(MockEngine {
            attempts++
            respond(content = ByteReadChannel(""), status = HttpStatusCode.InternalServerError)
        })

        //When
        val result = safeApi.call { client.get("https://test/") }

        //Then
        assertIs<CallResult.HttpError>(result)
        assertEquals(500, result.code)
        assertEquals(3, attempts)
    }

    @Test
    fun callRecoversAfterServerErrorOnRetry() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3)
        var attempts = 0
        val client = mockClient(MockEngine {
            attempts++
            if (attempts < 2) {
                respond(content = ByteReadChannel(""), status = HttpStatusCode.InternalServerError)
            } else {
                respond(content = ByteReadChannel(""), status = HttpStatusCode.OK)
            }
        })

        //When
        val result = safeApi.call { client.get("https://test/") }

        //Then
        assertIs<CallResult.Success<*>>(result)
        assertEquals(2, attempts)
    }

    @Test
    fun callRetriesNetworkErrorAndReturnsNetworkErrorAfterExhaustingAttempts() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3)
        var attempts = 0

        //When
        val result = safeApi.call {
            attempts++
            throw IOException("connection reset")
        }

        //Then
        assertIs<CallResult.NetworkError>(result)
        assertEquals(3, attempts)
    }

    @Test
    fun callRecoversAfterNetworkErrorOnRetry() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3)
        var attempts = 0

        //When
        val result = safeApi.call {
            attempts++
            if (attempts < 2) throw IOException("timeout") else "ok"
        }

        //Then
        assertEquals(CallResult.Success("ok"), result)
        assertEquals(2, attempts)
    }

    @Test
    fun callReturnsOtherErrorWithoutRetryOnUnexpectedException() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3)
        var attempts = 0

        //When
        val result = safeApi.call {
            attempts++
            throw IllegalStateException("boom")
        }

        //Then
        assertIs<CallResult.OtherError>(result)
        assertEquals(1, attempts)
    }

    @Test
    fun callDoesNotRetryWhenMaxAttemptIsOne() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 1)
        var attempts = 0

        //When
        val result = safeApi.call {
            attempts++
            throw IOException("timeout")
        }

        //Then
        assertIs<CallResult.NetworkError>(result)
        assertEquals(1, attempts)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun callWaitsIncreasingBackoffBetweenRetries() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3, attemptDelay = 100.milliseconds)

        //When
        safeApi.call { throw IOException("timeout") }

        //Then
        // attempt1 fails at t=0 -> delay 100ms*1; attempt2 fails at t=100 -> delay 100ms*2;
        // attempt3 fails, no more delay
        assertEquals(300, testScheduler.currentTime)
    }

    @Test
    fun concurrentCallsResolveIndependentlyWithoutSharedState() = runTest {
        //Given
        val safeApi = createSafeApi(maxAttempt = 3, attemptDelay = 50.milliseconds)
        var slowAttempts = 0
        val slowCallStarted = CompletableDeferred<Unit>()

        //When
        val slowDeferred = async {
            safeApi.call {
                slowAttempts++
                if (slowAttempts == 1) slowCallStarted.complete(Unit)
                if (slowAttempts < 3) throw IOException("timeout") else "slow-ok"
            }
        }
        slowCallStarted.await()
        val fastResult = safeApi.call { "fast-ok" }
        val slowResult = slowDeferred.await()

        //Then
        assertEquals(CallResult.Success("fast-ok"), fastResult)
        assertEquals(CallResult.Success("slow-ok"), slowResult)
        assertEquals(3, slowAttempts)
    }

    private fun createSafeApi(
        maxAttempt: Int = 3,
        attemptDelay: Duration = 1.milliseconds
    ): SafeApi = SafeApiImpl(
        maxAttempt = maxAttempt,
        attemptDelay = attemptDelay
    )

    private fun mockClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        expectSuccess = true
    }
}
