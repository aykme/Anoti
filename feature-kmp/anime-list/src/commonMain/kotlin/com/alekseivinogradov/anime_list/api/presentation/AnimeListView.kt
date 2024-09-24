package com.alekseivinogradov.anime_list.api.presentation

import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anime_list.api.presentation.model.UiModel
import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeListView : MviView<UiModel, AnimeListMainStore.Intent>