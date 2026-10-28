package com.alekseivinogradov.anoti.infodialog.kmp.api.domain.store

import com.arkivanov.mvikotlin.core.store.Store

/** The information dialog: whether it shows, and the pages its links open. */
interface InfoDialogStore :
    Store<InfoDialogStore.Intent, InfoDialogStore.State, InfoDialogStore.Label> {

    /** @param isShown whether the dialog is on screen. */
    data class State(val isShown: Boolean = false)

    /** What the user does with the dialog and its button. */
    sealed interface Intent {
        /** The user tapped the info button; also replayed when a saved open dialog is restored. */
        data object InfoClick : Intent

        /** The user tapped Close, pressed Back or tapped outside the dialog. */
        data object CloseClick : Intent

        /** The user tapped the privacy policy link. */
        data object PrivacyPolicyClick : Intent

        /** The user tapped the support link. */
        data object SupportClick : Intent
    }

    /** None: nothing outside the dialog reacts to it. */
    sealed interface Label

    /** None: the store bootstraps nothing. */
    sealed interface Action

    /** What the executor tells the reducer. */
    sealed interface Message {
        /** Replaces [State.isShown]. */
        data class ChangeVisibility(val isShown: Boolean) : Message
    }
}
