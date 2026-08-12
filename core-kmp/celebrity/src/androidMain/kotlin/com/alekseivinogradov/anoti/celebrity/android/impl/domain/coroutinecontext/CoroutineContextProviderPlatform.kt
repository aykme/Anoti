package com.alekseivinogradov.anoti.celebrity.android.impl.domain.coroutinecontext

import android.content.Context
import android.util.Log
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.toast.manager.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase

class CoroutineContextProviderPlatform(
    appContext: Context
) : CoroutineContextProviderBase() {

    private val tag = "Exception Handler"

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        Log.e(tag, "$throwable")
        ToastManager.makeUnknownErrorToast(appContext.applicationContext)
    }
}
