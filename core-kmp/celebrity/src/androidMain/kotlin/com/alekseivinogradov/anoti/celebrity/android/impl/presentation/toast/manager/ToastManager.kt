package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.toast.manager

import android.content.Context
import android.widget.Toast
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderKmp
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

object ToastManager {

    private val coroutineContextProvider = CoroutineContextProviderKmp()

    private val connectionErrorText: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.connection_error)
        }

    private val unknownErrorText: String =
        runBlocking(coroutineContextProvider.ioDispatcher) {
            getString(Res.string.unknown_error)
        }

    private fun makeLongToast(appContext: Context, text: String) {
        Toast.makeText(
            /* context = */ appContext.applicationContext,
            /* text = */ text,
            /* duration = */ Toast.LENGTH_LONG
        ).show()
    }

    fun makeConnectionErrorToast(appContext: Context) {
        makeLongToast(appContext = appContext, text = connectionErrorText)
    }

    fun makeUnknownErrorToast(appContext: Context) {
        makeLongToast(appContext = appContext, text = unknownErrorText)
    }
}
