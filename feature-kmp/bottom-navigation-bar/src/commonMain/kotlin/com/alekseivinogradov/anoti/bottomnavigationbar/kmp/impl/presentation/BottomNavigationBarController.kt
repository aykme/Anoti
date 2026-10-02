package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.BottomNavigationBarView
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.mapper.mapStateToUiModel
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

/**
 * Wires [AnimeDatabaseStore] to [BottomNavigationBarStore] for the root's lifetime, and a view to
 * the store for the view's own. The root that owns the stores builds it once and disposes them.
 *
 * @param lifecycle the root's lifecycle; the database feeds the badge while it is started.
 * @param mainStore the bottom navigation bar's own store.
 * @param animeDatabaseStore saved-anime database store; drives the favorites badge number.
 */
class BottomNavigationBarController(
    lifecycle: Lifecycle,
    private val mainStore: BottomNavigationBarStore,
    private val animeDatabaseStore: AnimeDatabaseStore
) {

    init {
        connectAllAuxiliaryStoresToMain(lifecycle)
    }

    /**
     * Binds [mainView] to the store while [viewLifecycle] lasts.
     *
     * @param mainView the view of one composition of the bar.
     * @param viewLifecycle the lifecycle of that composition.
     */
    fun onViewCreated(mainView: BottomNavigationBarView, viewLifecycle: Lifecycle) {
        connectMainStoreToMainView(mainView = mainView, viewLifecycle = viewLifecycle)
    }

    private fun connectAllAuxiliaryStoresToMain(lifecycle: Lifecycle) {
        bind(lifecycle, BinderLifecycleMode.START_STOP) {
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
