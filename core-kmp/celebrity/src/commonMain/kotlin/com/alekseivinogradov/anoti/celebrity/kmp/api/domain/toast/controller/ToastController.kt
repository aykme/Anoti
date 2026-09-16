package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.controller

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.jetbrains.compose.resources.StringResource

/**
 * App-wide stream of toast messages for the toast host on screen. Safe to call from any thread.
 * A message sent while no host is collecting is dropped.
 */
class ToastController {

    private val messageFlow = MutableSharedFlow<StringResource>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /** Messages in the order they were sent. A busy collector gets only the latest one. */
    val messages: Flow<StringResource> = messageFlow.asSharedFlow()

    fun show(message: StringResource) {
        messageFlow.tryEmit(message)
    }
}
