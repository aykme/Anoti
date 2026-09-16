package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.controller

import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
class ToastControllerTest {

    @Test
    fun messageShownWithoutCollectorIsDropped() = runTest {
        //Given
        val controller = ToastController()
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
        val controller = ToastController()
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
        val controller = ToastController()
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

    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun messagesFromConcurrentSendersAreDelivered() = runTest {
        //Given
        val controller = ToastController()
        val deliveredCount = AtomicInt(0)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.messages.collect { deliveredCount.incrementAndFetch() }
        }

        //When
        withContext(Dispatchers.Default) {
            repeat(SENDER_COUNT) { launch { controller.show(Res.string.unknown_error) } }
        }
        runCurrent()

        //Then
        assertTrue(deliveredCount.load() in 1..SENDER_COUNT)
    }

    private companion object {
        const val SENDER_COUNT = 1_000
    }
}
