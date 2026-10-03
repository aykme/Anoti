package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation

import com.alekseivinogradov.anoti.animedatabase.kmp.api.domain.store.AnimeDatabaseStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Wires [AnimeDatabaseStore] to [BottomNavigationBarStore] while the root is started, and hands
 * the store's labels to [onLabel]. The UI reads [state] and sends its taps through [accept]. The
 * root that owns the stores builds it once and disposes them.
 *
 * @param lifecycle the root's lifecycle; the wiring runs while it is started.
 * @param mainStore the bottom navigation bar's own store.
 * @param animeDatabaseStore saved-anime database store; drives the favorites badge number.
 * @param onLabel acts on a label the store publishes, such as a request to switch screens.
 */
class BottomNavigationBarController(
    lifecycle: Lifecycle,
    private val mainStore: BottomNavigationBarStore,
    private val animeDatabaseStore: AnimeDatabaseStore,
    private val onLabel: (BottomNavigationBarStore.Label) -> Unit
) {

    /** The store's state, for as long as the root lives. */
    val state: StateFlow<BottomNavigationBarStore.State> = mainStore.stateFlow(lifecycle)

    init {
        bind(lifecycle, BinderLifecycleMode.START_STOP) {
            animeDatabaseStore.states.map(::mapDatabaseStoreStateToMainStoreIntent) bindTo mainStore
            mainStore.labels bindTo onLabel
        }
    }

    /** Sends a tap on the bar to the store. */
    fun accept(intent: BottomNavigationBarStore.Intent) {
        mainStore.accept(intent)
    }
}
