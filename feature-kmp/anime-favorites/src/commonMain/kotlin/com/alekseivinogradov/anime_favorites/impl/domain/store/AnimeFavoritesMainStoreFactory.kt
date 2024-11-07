package com.alekseivinogradov.anime_favorites.impl.domain.store

import com.alekseivinogradov.anime_base.api.domain.ToastProvider
import com.alekseivinogradov.anime_favorites.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anime_favorites.impl.domain.usecase.wrapper.FavoritesUsecases
import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnimeFavoritesMainStoreFactory(
    private val storeFactory: StoreFactory,
    coroutineContextProvider: CoroutineContextProvider,
    usecases: FavoritesUsecases,
    toastProvider: ToastProvider
) {
    private val executorFactory: () -> AnimeFavoritesExecutorImpl = {
        AnimeFavoritesExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            usecases = usecases,
            toastProvider = toastProvider
        )
    }

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
