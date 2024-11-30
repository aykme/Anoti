package com.alekseivinogradov.anime_favorites.impl.presentation

import com.alekseivinogradov.anime_favorites.api.domain.mapper.mapDatabaseStoreLabelToMainStoreIntent
import com.alekseivinogradov.anime_favorites.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_favorites.api.domain.mapper.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anime_favorites.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

class AnimeFavoritesController(
    lifecycle: Lifecycle,
    private val mainStore: AnimeFavoritesMainStore,
    private val animeDatabaseStore: AnimeDatabaseStore,
) {

    init {
        lifecycle.doOnDestroy { animeDatabaseStore.dispose() }
        lifecycle.doOnDestroy { mainStore.dispose() }
    }

    fun onViewCreated(mainView: AnimeFavoritesView, viewLifecycle: Lifecycle) {
        connectAllAuxiliaryStoresToMain(viewLifecycle)
        connectMainStoreToMainView(mainView = mainView, viewLifecycle = viewLifecycle)
    }

    private fun connectAllAuxiliaryStoresToMain(viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            animeDatabaseStore.states.map(
                ::mapDatabaseStoreStateToMainStoreIntent
            ) bindTo mainStore
            animeDatabaseStore.labels.map(
                ::mapDatabaseStoreLabelToMainStoreIntent
            ) bindTo mainStore
            mainStore.labels.map(
                ::mapMainStoreLabelToDatabaseStoreIntent
            ) bindTo animeDatabaseStore
        }
    }

    private fun connectMainStoreToMainView(
        mainView: AnimeFavoritesView,
        viewLifecycle: Lifecycle
    ) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            mainStore.states.map(::mapStateToUiModel) bindTo mainView
            mainView.events bindTo mainStore
        }
    }
}
