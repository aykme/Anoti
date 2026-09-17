package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CoroutineExceptionHandler

class CoroutineContextProviderDefaultImplTest {

    @Test
    fun uncaughtThrowableShowsUnknownErrorToastOnly() {
        //Given
        var connectionErrorCount = 0
        var unknownErrorCount = 0
        val toastProvider = ToastProvider(
            makeConnectionErrorToast = { connectionErrorCount++ },
            makeUnknownErrorToast = { unknownErrorCount++ }
        )
        val provider = CoroutineContextProviderDefaultImpl(toastProvider = toastProvider)
        val handler = assertNotNull(provider.mainCoroutineContext[CoroutineExceptionHandler])

        //When
        handler.handleException(EmptyCoroutineContext, IllegalStateException("boom"))

        //Then
        assertEquals(1, unknownErrorCount)
        assertEquals(0, connectionErrorCount)
    }
}
