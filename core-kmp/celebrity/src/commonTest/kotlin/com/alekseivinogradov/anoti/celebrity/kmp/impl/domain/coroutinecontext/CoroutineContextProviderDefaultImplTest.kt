package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CoroutineExceptionHandler

class CoroutineContextProviderDefaultImplTest {

    @Test
    fun uncaughtThrowableShowsUnknownErrorSystemMessageOnly() {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        val systemMessageProvider = SystemMessageProvider(
            makeConnectionErrorSystemMessage = { connectionErrorCount++ },
            makeUnknownErrorSystemMessage = { unknownErrorCount++ }
        )
        val provider = CoroutineContextProviderDefaultImpl(
            systemMessageProvider = systemMessageProvider
        )
        val handler = assertNotNull(provider.mainCoroutineContext[CoroutineExceptionHandler])

        //When
        handler.handleException(EmptyCoroutineContext, IllegalStateException("boom"))

        //Then
        assertEquals(1, unknownErrorCount)
        assertEquals(0, connectionErrorCount)
    }
}
