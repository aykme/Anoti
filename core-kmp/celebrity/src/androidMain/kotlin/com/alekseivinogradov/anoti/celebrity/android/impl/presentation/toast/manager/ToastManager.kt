package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.toast.manager

import android.content.Context
import android.widget.Toast
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderKmp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

object ToastManager {
    private val scope by lazy {
        CoroutineScope(CoroutineContextProviderKmp().mainCoroutineContext)
    }

    private fun makeLongToast(appContext: Context, text: String) {
        Toast.makeText(
            /* context = */
            appContext.applicationContext,
            /* text = */
            text,
            /* duration = */
            Toast.LENGTH_LONG
        ).show()
    }

    fun makeConnectionErrorToast(appContext: Context) {
        scope.launch {
            makeLongToast(
                appContext = appContext,
                text = getString(Res.string.connection_error)
            )
        }
    }

    fun makeUnknownErrorToast(appContext: Context) {
        scope.launch {
            makeLongToast(
                appContext = appContext,
                text = getString(Res.string.unknown_error)
            )
        }
    }
}
