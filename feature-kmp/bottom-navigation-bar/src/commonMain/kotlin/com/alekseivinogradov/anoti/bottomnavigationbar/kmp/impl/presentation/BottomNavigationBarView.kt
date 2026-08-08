package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

/**
 * The view contract the platform layer implements to render the store's state and forward
 * navigation labels.
 */
interface BottomNavigationBarView : MviView<UiModel, BottomNavigationBarStore.Intent> {

    /** Handles a navigation [label] emitted by the store (e.g. switch the displayed screen). */
    fun handle(label: BottomNavigationBarStore.Label)
}
