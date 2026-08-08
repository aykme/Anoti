package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

/**
 * Wires [BottomNavigationBarStore] to its view and to [AnimeDatabaseStore] for the bar's
 * lifecycle.
 *
 * @param lifecycle screen lifecycle the store bindings are tied to.
 * @param mainStore the bottom navigation bar's own store.
 * @param animeDatabaseStore saved-anime database store; drives the favorites badge number.
 */
class BottomNavigationBarController(
    lifecycle: Lifecycle,
    private val mainStore: BottomNavigationBarStore,
    private val animeDatabaseStore: AnimeDatabaseStore
) {

    init {
        lifecycle.doOnDestroy { mainStore.dispose() }
        lifecycle.doOnDestroy { animeDatabaseStore.dispose() }
    }

    /**
     * Binds [mainView] to the store for [viewLifecycle]'s duration.
     *
     * @param mainView view instance created for this lifecycle.
     * @param viewLifecycle the view's own lifecycle.
     */
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
