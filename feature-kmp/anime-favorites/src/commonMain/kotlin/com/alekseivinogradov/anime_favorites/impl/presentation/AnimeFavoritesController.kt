package com.alekseivinogradov.anime_favorites.impl.presentation

import com.alekseivinogradov.anime_favorites.api.domain.mapper.mapDatabaseStoreStateToMainStoreIntent
import com.alekseivinogradov.anime_favorites.api.presentation.AnimeFavoritesView
import com.alekseivinogradov.anime_favorites.api.presentation.mapper.mapStateToUiModel
import com.alekseivinogradov.anime_favorites.impl.domain.store.AnimeFavoritesMainStoreFactory
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.impl.domain.store.DatabaseStoreFactory
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.flow.map

class AnimeFavoritesController(
    storeFactory: StoreFactory,
    lifecycle: Lifecycle,
    databaseRepository: AnimeDatabaseRepository
) {

    private val mainStore = AnimeFavoritesMainStoreFactory(
        storeFactory = storeFactory
    ).create()

    private val databaseStore = DatabaseStoreFactory(
        storeFactory = storeFactory,
        repository = databaseRepository
    ).create()

    init {
        lifecycle.doOnDestroy { mainStore.dispose() }
    }

    fun onViewCreated(mainView: AnimeFavoritesView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            databaseStore.states.map(::mapDatabaseStoreStateToMainStoreIntent) bindTo mainStore
            mainStore.states.map(::mapStateToUiModel) bindTo mainView
        }
    }
}
