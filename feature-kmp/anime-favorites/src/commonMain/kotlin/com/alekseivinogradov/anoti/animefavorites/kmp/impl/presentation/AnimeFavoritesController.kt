package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapDatabaseStoreLabelToMainStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.mapper.mapStateToUiModel
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

/**
 * Wires [AnimeFavoritesMainStore] to its view and to [AnimeDatabaseStore] for the screen's
 * lifecycle.
 *
 * @param lifecycle screen lifecycle the store bindings are tied to.
 * @param mainStore the favorites screen's own store.
 * @param animeDatabaseStore saved-anime database store; the source of the favorites list.
 */
class AnimeFavoritesController(
    lifecycle: Lifecycle,
    private val mainStore: AnimeFavoritesMainStore,
    private val animeDatabaseStore: AnimeDatabaseStore,
) {

    init {
        lifecycle.doOnDestroy { animeDatabaseStore.dispose() }
        lifecycle.doOnDestroy { mainStore.dispose() }
    }

    /**
     * Binds [mainView] to the store for [viewLifecycle]'s duration.
     *
     * @param mainView view instance created for this lifecycle.
     * @param viewLifecycle the view's own lifecycle.
     */
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
