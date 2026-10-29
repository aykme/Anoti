package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.PRIVACY_POLICY_URL
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.SUPPORT_URL
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogExecutor
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.usecase.wrapper.InfoDialogUsecases
import kotlinx.coroutines.launch

private const val TAG = "InfoDialog"

/**
 * Shows and closes the information dialog, and hands each link's page to the system browser.
 * A page no app opens shows the unknown-error message; the dialog stays open either way.
 *
 * @param coroutineContextProvider gives the main context the page requests run in.
 * @param usecases what the links call.
 * @param systemMessageProvider shows the message when no app opens a page.
 */
class InfoDialogExecutorImpl(
    coroutineContextProvider: CoroutineContextProvider,
    private val usecases: InfoDialogUsecases,
    private val systemMessageProvider: SystemMessageProvider
) : InfoDialogExecutor(mainContext = coroutineContextProvider.newMainCoroutineContext()) {

    override fun executeIntent(intent: InfoDialogStore.Intent) {
        when (intent) {
            InfoDialogStore.Intent.InfoClick -> show()
            InfoDialogStore.Intent.CloseClick -> close()
            InfoDialogStore.Intent.PrivacyPolicyClick -> openPrivacyPolicy()
            InfoDialogStore.Intent.SupportClick -> openSupport()
        }
    }

    private fun show() {
        if (state().isShown) return
        dispatch(InfoDialogStore.Message.ChangeVisibility(isShown = true))
        println("$ANOTI_TAG $TAG: the information dialog is shown")
    }

    private fun close() {
        if (!state().isShown) return
        dispatch(InfoDialogStore.Message.ChangeVisibility(isShown = false))
        println("$ANOTI_TAG $TAG: the information dialog is closed")
    }

    private fun openPrivacyPolicy() {
        println("$ANOTI_TAG $TAG: the privacy policy page is requested")
        openPage(PRIVACY_POLICY_URL)
    }

    private fun openSupport() {
        println("$ANOTI_TAG $TAG: the support page is requested")
        openPage(SUPPORT_URL)
    }

    private fun openPage(url: String) {
        scope.launch {
            if (!usecases.openWebPageUsecase.execute(url)) {
                println("$ANOTI_TAG $TAG: no app opened the page")
                systemMessageProvider.makeUnknownErrorSystemMessage()
            }
        }
    }
}
