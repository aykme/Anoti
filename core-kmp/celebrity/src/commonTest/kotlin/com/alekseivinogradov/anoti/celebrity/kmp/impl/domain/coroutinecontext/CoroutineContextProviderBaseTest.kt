package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class CoroutineContextProviderBaseTest {

    private fun createProvider(): CoroutineContextProviderBase = CoroutineContextProviderBareImpl()

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
    fun theBareProvidersHandlerReportsAThrowableWithoutRethrowingIt() {
        //Given
        val provider = CoroutineContextProviderBareImpl()

        //When
        provider.exceptionHandlerCallback(IllegalStateException("boom"))

        //Then
        // Reaching here is the assertion: this provider exists for code that must not show UI,
        // so its handler has nowhere to report to and nothing to throw.
        assertNotNull(provider.mainCoroutineContext[CoroutineExceptionHandler])
    }
}
