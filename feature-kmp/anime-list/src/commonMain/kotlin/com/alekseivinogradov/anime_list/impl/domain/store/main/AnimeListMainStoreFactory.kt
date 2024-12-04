package com.alekseivinogradov.anime_list.impl.domain.store.main

import com.alekseivinogradov.anime_list.api.domain.store.main.AnimeListMainStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnimeListMainStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: AnimeListExecutorFactory
) {
    fun create(): AnimeListMainStore {
        return object : AnimeListMainStore,
            Store<AnimeListMainStore.Intent, AnimeListMainStore.State, AnimeListMainStore.Label>
            by storeFactory.create(
                name = "AnimeListMainStore",
                initialState = AnimeListMainStore.State(),
                bootstrapper = SimpleBootstrapper(),
                executorFactory = executorFactory,
                reducer = AnimeListReducerImpl()
            ) {}
    }
}
