package com.alekseivinogradov.bottom_navigation_bar.impl.presentation

import com.alekseivinogradov.bottom_navigation_bar.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.bottom_navigation_bar.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.bottom_navigation_bar.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

class BottomNavigationBarController(
    lifecycle: Lifecycle,
    private val mainStore: BottomNavigationBarStore,
    private val animeDatabaseStore: AnimeDatabaseStore
) {

    init {
        lifecycle.doOnDestroy { mainStore.dispose() }
        lifecycle.doOnDestroy { animeDatabaseStore.dispose() }
    }

    fun onViewCreated(mainView: BottomNavigationBarView, viewLifecycle: Lifecycle) {
        connectAllAuxiliaryStoresToMain(viewLifecycle)
        connectMainStoreToMainView(mainView = mainView, viewLifecycle = viewLifecycle)
    }

    private fun connectAllAuxiliaryStoresToMain(viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            animeDatabaseStore.states.map(::mapDatabaseStoreStateToMainStoreIntent) bindTo mainStore
        }
    }

    private fun connectMainStoreToMainView(
        mainView: BottomNavigationBarView,
        viewLifecycle: Lifecycle
    ) {
        bind(viewLifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            mainView.events bindTo mainStore
            mainStore.states.map(::mapStateToUiModel) bindTo mainView
            mainStore.labels bindTo mainView::handle
        }
    }
}
