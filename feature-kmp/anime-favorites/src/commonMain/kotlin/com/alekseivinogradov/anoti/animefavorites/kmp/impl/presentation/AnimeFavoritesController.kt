package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapDatabaseStoreLabelToMainStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.mapper.mapMainStoreLabelToDatabaseStoreIntent
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Wires [AnimeFavoritesMainStore] to [AnimeDatabaseStore] while the screen component is started.
 * The UI reads [state] and sends its events through [accept]. The component that owns the stores
 * builds it once and disposes them.
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

    /** The store's state, for as long as the screen component lives. */
    val state: StateFlow<AnimeFavoritesMainStore.State> = mainStore.stateFlow(lifecycle)

    init {
        connectAllAuxiliaryStoresToMain(lifecycle)
    }

    /** Sends an event of the screen to the store. */
    fun accept(intent: AnimeFavoritesMainStore.Intent) {
        mainStore.accept(intent)
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
}
