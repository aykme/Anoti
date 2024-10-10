package com.alekseivinogradov.anime_favorites.api.presentation

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeFavoritesView : MviView<UiModel, AnimeFavoritesMainStore.Intent>