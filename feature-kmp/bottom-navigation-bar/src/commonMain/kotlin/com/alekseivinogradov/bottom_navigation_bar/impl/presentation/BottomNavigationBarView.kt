package com.alekseivinogradov.bottom_navigation_bar.impl.presentation

import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface BottomNavigationBarView : MviView<UiModel, BottomNavigationBarStore.Intent> {

    fun handle(label: BottomNavigationBarStore.Label)
}
