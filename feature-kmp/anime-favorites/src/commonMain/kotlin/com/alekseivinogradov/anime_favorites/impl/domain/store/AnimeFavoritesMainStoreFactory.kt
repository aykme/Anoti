package com.alekseivinogradov.anime_favorites.impl.domain.store

import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnimeFavoritesMainStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: () -> AnimeFavoritesExecutorImpl,
) {

    fun create(): AnimeFavoritesMainStore {
        return object : AnimeFavoritesMainStore,
            Store<
                    AnimeFavoritesMainStore.Intent,
                    AnimeFavoritesMainStore.State,
                    AnimeFavoritesMainStore.Label
                    >
            by storeFactory.create(
                name = "AnimeFavoritesMainStore",
                initialState = AnimeFavoritesMainStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = executorFactory,
                reducer = AnimeFavoritesReducerImpl()
            ) {}
    }
}
