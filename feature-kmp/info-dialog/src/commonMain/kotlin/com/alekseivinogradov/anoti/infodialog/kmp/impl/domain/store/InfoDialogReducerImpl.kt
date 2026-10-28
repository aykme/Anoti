package com.alekseivinogradov.anoti.infodialog.kmp.impl.domain.store

import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store.InfoDialogStore
import com.arkivanov.mvikotlin.core.store.Reducer

/** Applies the information dialog's messages to its state. */
internal class InfoDialogReducerImpl : Reducer<InfoDialogStore.State, InfoDialogStore.Message> {
    override fun InfoDialogStore.State.reduce(msg: InfoDialogStore.Message): InfoDialogStore.State =
        when (msg) {
            is InfoDialogStore.Message.ChangeVisibility -> copy(isShown = msg.isShown)
        }
}
