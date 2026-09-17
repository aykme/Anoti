package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.controller

import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
class SystemMessageControllerTest {

    @Test
    fun messageShownWithoutCollectorIsDropped() = runTest {
        //Given
        val controller = SystemMessageController()
        val received = mutableListOf<StringResource>()

        //When
        controller.show(Res.string.connection_error)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.messages.toList(received)
        }
        runCurrent()

        //Then
        assertTrue(received.isEmpty(), "a host that appears later must not replay old errors")
    }

    @Test
    fun collectorReceivesMessagesInOrder() = runTest {
        //Given
        val controller = SystemMessageController()
        val received = mutableListOf<StringResource>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.messages.toList(received)
        }

        //When
        controller.show(Res.string.connection_error)
        controller.show(Res.string.unknown_error)

        //Then
        assertEquals(listOf(Res.string.connection_error, Res.string.unknown_error), received)
    }

    @Test
    fun busyCollectorReceivesOnlyLatestMessage() = runTest {
        //Given
        val controller = SystemMessageController()
        val received = mutableListOf<StringResource>()
        backgroundScope.launch(StandardTestDispatcher(testScheduler)) {
            controller.messages.toList(received)
        }
        runCurrent()

        //When
        controller.show(Res.string.connection_error)
        controller.show(Res.string.unknown_error)
        runCurrent()

        //Then
        assertEquals(listOf(Res.string.unknown_error), received)
    }
}
