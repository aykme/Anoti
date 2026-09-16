package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.toast.manager

import android.content.Context
import android.widget.Toast
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderKmp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

object ToastManager {
    private val scope by lazy {
        CoroutineScope(CoroutineContextProviderKmp().mainCoroutineContext)
    }

    private var job: Job? = null

    private var toast: Toast? = null

    private fun makeLongToast(appContext: Context, text: String) {
        // Once shown, a toast sits in the system queue until it expires. Cancelling it keeps a
        // burst of errors down to one message instead of a chain.
        toast?.cancel()
        toast = Toast.makeText(
            /* context = */
            appContext.applicationContext,
            /* text = */
            text,
            /* duration = */
            Toast.LENGTH_LONG
        ).also { newToast: Toast -> newToast.show() }
    }

    fun makeConnectionErrorToast(appContext: Context) {
        showError(appContext = appContext, resource = Res.string.connection_error)
    }

    fun makeUnknownErrorToast(appContext: Context) {
        showError(appContext = appContext, resource = Res.string.unknown_error)
    }

    /**
     * The whole swap runs inside [scope] so that [job] and [toast] are only ever touched on the
     * main dispatcher — the coroutine exception handler reaches this from arbitrary threads.
     */
    private fun showError(appContext: Context, resource: StringResource) {
        scope.launch {
            job?.cancel()
            job = launch {
                makeLongToast(
                    appContext = appContext,
                    text = getString(resource)
                )
            }
        }
    }
}
