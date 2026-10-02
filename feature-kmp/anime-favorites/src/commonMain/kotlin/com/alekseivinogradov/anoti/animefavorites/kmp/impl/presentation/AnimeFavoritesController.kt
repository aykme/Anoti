package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapDatabaseStoreLabelToMainStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.mapper.mapStateToUiModel
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

/**
 * Wires [AnimeFavoritesMainStore] to [AnimeDatabaseStore] for the screen component's lifetime,
 * and a view to the store for the view's own. The component that owns the stores builds it once
 * and disposes them.
 *
 * @param lifecycle the screen component's lifecycle; the stores are wired while it is started.
 * @param mainStore the favorites screen's own store.
 * @param animeDatabaseStore saved-anime database store; the source of the favorites list.
 */
class AnimeFavoritesController(
    lifecycle: Lifecycle,
    private val mainStore: AnimeFavoritesMainStore,
    private val animeDatabaseStore: AnimeDatabaseStore,
) {

    init {
        connectAllAuxiliaryStoresToMain(lifecycle)
    }

    /**
     * Binds [mainView] to the store while [viewLifecycle] is started.
     *
     * @param mainView the view of one composition of the screen.
     * @param viewLifecycle the lifecycle of that composition.
     */
    fun onViewCreated(mainView: AnimeFavoritesView, viewLifecycle: Lifecycle) {
        connectMainStoreToMainView(mainView = mainView, viewLifecycle = viewLifecycle)
    }

    private fun connectAllAuxiliaryStoresToMain(lifecycle: Lifecycle) {
        bind(lifecycle, BinderLifecycleMode.START_STOP) {
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
