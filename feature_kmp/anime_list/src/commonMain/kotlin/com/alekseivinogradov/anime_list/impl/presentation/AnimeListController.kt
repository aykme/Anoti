package com.alekseivinogradov.anime_list.impl.presentation

import com.alekseivinogradov.anime_list.api.presentation.AnimeListView
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class AnimeListController(lifecycle: Lifecycle) {

    init {

    }

    fun onViewCreated(mainView: AnimeListView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            subscribeOnAllRequiredStates() bindTo mainView
        }
    }

    private fun subscribeOnAllRequiredStates(): Flow<AnimeListView.UiModel> {
        return emptyFlow<AnimeListView.UiModel>()
    }
}