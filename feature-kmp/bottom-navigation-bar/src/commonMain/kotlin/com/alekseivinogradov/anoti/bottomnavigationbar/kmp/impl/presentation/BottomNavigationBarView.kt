package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface BottomNavigationBarView : MviView<UiModel, BottomNavigationBarStore.Intent> {

    fun handle(label: BottomNavigationBarStore.Label)
}
