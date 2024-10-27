package com.alekseivinogradov.bottom_navigation_bar.impl.presentation

import com.alekseivinogradov.bottom_navigation_bar.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.bottom_navigation_bar.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.bottom_navigation_bar.impl.domain.store.BottomNavigationBarStoreFactory
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.impl.domain.store.DatabaseStoreFactory
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

class BottomNavigationBarController(
    storeFactory: StoreFactory,
    lifecycle: Lifecycle,
    coroutineContextProvider: CoroutineContextProvider,
    databaseRepository: AnimeDatabaseRepository
) {

    private val mainStore = BottomNavigationBarStoreFactory(
        storeFactory = storeFactory
    ).create()

    private val databaseStore = DatabaseStoreFactory(
        storeFactory = storeFactory,
        coroutineContextProvider = coroutineContextProvider,
        repository = databaseRepository
    ).create()

    init {
        lifecycle.doOnDestroy { databaseStore.dispose() }
        lifecycle.doOnDestroy { mainStore.dispose() }
    }

    fun onViewCreated(mainView: BottomNavigationBarView, viewLifecycle: Lifecycle) {
        connectAllAuxiliaryStoresToMain(viewLifecycle)
        connectMainStoreToMainView(mainView = mainView, viewLifecycle = viewLifecycle)
    }

    private fun connectAllAuxiliaryStoresToMain(viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            databaseStore.states.map(::mapDatabaseStoreStateToMainStoreIntent) bindTo mainStore
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
