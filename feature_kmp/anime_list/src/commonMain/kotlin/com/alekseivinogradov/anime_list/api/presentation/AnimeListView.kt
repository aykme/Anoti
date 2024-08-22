package com.alekseivinogradov.anime_list.api.presentation

import com.arkivanov.mvikotlin.core.view.MviView

interface AnimeListView : MviView<AnimeListView.UiModel, AnimeListView.UiEvent> {

    class UiModel()

    sealed interface UiEvent
}