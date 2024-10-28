package com.alekseivinogradov.celebrity.impl.domain.coroutine_context

import android.content.Context
import android.util.Log
import com.alekseivinogradov.celebrity.impl.presentation.toast.AnotiToast

class CoroutineContextProviderPlatform(context: Context) : CoroutineContextProviderBase() {

    private val tag = "Exception Handler"

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        Log.e(tag, "$throwable")
        AnotiToast.makeUnknownErrorToast(context.applicationContext)
    }
}
