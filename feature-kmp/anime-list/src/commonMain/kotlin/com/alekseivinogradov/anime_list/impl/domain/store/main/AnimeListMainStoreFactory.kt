package com.alekseivinogradov.anime_list.impl.domain.store.main

import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

internal class AnimeListMainStoreFactory(
    private val storeFactory: StoreFactory
) {

    internal fun create(): AnimeListMainStore {
        return object : AnimeListMainStore,
            Store<AnimeListMainStore.Intent, AnimeListMainStore.State, AnimeListMainStore.Label>
            by storeFactory.create(
                name = "AnimeListMainStore",
                initialState = AnimeListMainStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = ::AnimeListExecutorImpl,
                reducer = AnimeListReducerImpl()
            ) {}
    }
}