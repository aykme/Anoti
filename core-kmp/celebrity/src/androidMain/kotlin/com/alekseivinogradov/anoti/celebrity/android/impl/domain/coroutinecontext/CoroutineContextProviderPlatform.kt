package com.alekseivinogradov.anoti.celebrity.android.impl.domain.coroutinecontext

import android.content.Context
import android.util.Log
import com.alekseivinogradov.anoti.celebrity.android.impl.presentation.toast.manager.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import com.alekseivinogradov.anoti.di.android.api.presentation.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoroutineContextProviderPlatform @Inject constructor(
    @AppContext appContext: Context
) : CoroutineContextProviderBase() {

    private val tag = "Exception Handler"

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        Log.e(tag, "$throwable")
        ToastManager.makeUnknownErrorToast(appContext.applicationContext)
    }
}
