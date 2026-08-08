package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeFavoritesView : MviView<UiModel, AnimeFavoritesMainStore.Intent>
