package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

/**
 * The view contract the platform layer implements to render the store's state.
 */
interface AnimeFavoritesView : MviView<UiModel, AnimeFavoritesMainStore.Intent>
