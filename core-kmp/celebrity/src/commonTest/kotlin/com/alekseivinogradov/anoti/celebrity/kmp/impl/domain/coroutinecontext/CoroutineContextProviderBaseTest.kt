package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class CoroutineContextProviderBaseTest {

    private val reported = mutableListOf<Throwable>()

    private fun createProvider(): CoroutineContextProviderBase =
        object : CoroutineContextProviderBase() {
            override val exceptionHandlerCallback: (Throwable) -> Unit = { reported += it }
        }

    @Test
    fun newMainCoroutineContextCarriesAFreshJobOnEveryCall() {
        //Given
        val provider = createProvider()

        //When
        val first = provider.newMainCoroutineContext()[Job]
        val second = provider.newMainCoroutineContext()[Job]

        //Then
        assertNotNull(first, "a scope built from it would own nothing")
        assertNotNull(second, "a scope built from it would own nothing")
        assertNotSame(first, second, "two scopes would share one job and cancel each other")
    }

    @Test
    fun appMainCoroutineContextCarriesAJob() {
        //Given
        val provider = createProvider()

        //When
        val job = provider.appMainCoroutineContext[Job]

        //Then
        assertNotNull(job, "work launched with it would not outlive the scope that started it")
    }

    @Test
    fun mainCoroutineContextCarriesNoJob() {
        //Given
        val provider = createProvider()

        //When
        val job = provider.mainCoroutineContext[Job]

        //Then
        assertNull(job, "a job here would reparent everything launched with it")
    }

    @Test
    fun workManagerCoroutineContextCarriesNoJob() {
        //Given
        val provider = createProvider()

        //When
        val job = provider.workManagerCoroutineContext[Job]

        //Then
        assertNull(job, "the worker's own cancellation would stop reaching the work it started")
    }

    @Test
    fun anUncaughtThrowableOnTheMainContextReachesTheCallback() {
        //Given
        val provider = createProvider()
        val handler = assertNotNull(provider.mainCoroutineContext[CoroutineExceptionHandler])
        val failure = IllegalStateException("boom")

        //When
        handler.handleException(EmptyCoroutineContext, failure)

        //Then
        assertEquals(listOf<Throwable>(failure), reported)
    }

    @Test
    fun anUncaughtThrowableOnTheWorkManagerContextIsSwallowed() {
        //Given
        val provider = createProvider()
        val handler = assertNotNull(
            provider.workManagerCoroutineContext[CoroutineExceptionHandler]
        )

        //When
        handler.handleException(EmptyCoroutineContext, IllegalStateException("boom"))

        //Then
        // A worker answers through its own result. Reporting here would put a message on a
        // screen nobody is looking at.
        assertEquals(emptyList(), reported)
    }

    @Test
    fun theBareProviderReportsWithoutReachingAnyScreen() {
        //Given
        val provider = CoroutineContextProviderBareImpl()
        val handler = assertNotNull(provider.mainCoroutineContext[CoroutineExceptionHandler])

        //When
        handler.handleException(EmptyCoroutineContext, IllegalStateException("boom"))

        //Then
        // Its whole point is having nowhere to report to: it must neither throw nor need a UI.
        assertEquals(emptyList(), reported)
    }
}
