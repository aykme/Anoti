package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation

import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.AnimeFavoritesUiModel
import com.arkivanov.mvikotlin.core.view.MviView

/**
 * The view contract [com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.navigation.AnimeFavoritesRoute]
 * implements to render the store's state.
 */
interface AnimeFavoritesView : MviView<AnimeFavoritesUiModel, AnimeFavoritesMainStore.Intent>
