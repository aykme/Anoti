package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.controller.ToastController
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineContextProviderDefaultImplTest {

    @Test
    fun uncaughtThrowableShowsUnknownErrorToast() = runTest {
        //Given
        val controller = ToastController()
        val provider = CoroutineContextProviderDefaultImpl(toastController = controller)
        val received = mutableListOf<StringResource>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.messages.toList(received)
        }
        val handler = assertNotNull(provider.mainCoroutineContext[CoroutineExceptionHandler])

        //When
        handler.handleException(EmptyCoroutineContext, IllegalStateException("boom"))

        //Then
        assertEquals(listOf(Res.string.unknown_error), received)
    }
}
