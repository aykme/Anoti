package com.alekseivinogradov.anime_database.impl.domain.store

import com.alekseivinogradov.anime_database.api.domain.store.AnimeDatabaseStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class AnimeDatabaseStoreFactory(
    private val storeFactory: StoreFactory,
    private val executorFactory: () -> AnimeDatabaseExecutorImpl,
) {

    fun create(): AnimeDatabaseStore {
        return object : AnimeDatabaseStore,
            Store<AnimeDatabaseStore.Intent, AnimeDatabaseStore.State, AnimeDatabaseStore.Label>
            by storeFactory.create(
                name = "AnimeDatabaseStore",
                initialState = AnimeDatabaseStore.State(),
                bootstrapper = SimpleBootstrapper(AnimeDatabaseStore.Action.SubscribeToDatabase),
                executorFactory = executorFactory,
                reducer = AnimeDatabaseReducerImpl()
            ) {}
    }
}
