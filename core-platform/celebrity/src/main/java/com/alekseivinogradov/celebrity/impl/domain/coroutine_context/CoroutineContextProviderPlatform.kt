package com.alekseivinogradov.celebrity.impl.domain.coroutine_context

import android.content.Context
import android.util.Log
import com.alekseivinogradov.celebrity.impl.presentation.toast.AnotiToast
import com.alekseivinogradov.di.api.presentation.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoroutineContextProviderPlatform @Inject constructor(
    @AppContext appContext: Context
) : CoroutineContextProviderBase() {

    private val tag = "Exception Handler"

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        Log.e(tag, "$throwable")
        AnotiToast.makeUnknownErrorToast(appContext.applicationContext)
    }
}
