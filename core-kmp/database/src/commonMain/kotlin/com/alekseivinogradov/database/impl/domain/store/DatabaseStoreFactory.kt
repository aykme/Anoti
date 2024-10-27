package com.alekseivinogradov.database.impl.domain.store

import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import com.alekseivinogradov.database.api.domain.repository.AnimeDatabaseRepository
import com.alekseivinogradov.database.api.domain.store.DatabaseExecutor
import com.alekseivinogradov.database.api.domain.store.DatabaseStore
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory

class DatabaseStoreFactory(
    private val storeFactory: StoreFactory,
    coroutineContextProvider: CoroutineContextProvider,
    repository: AnimeDatabaseRepository
) {
    private val executorFactory: () -> DatabaseExecutor = {
        DatabaseExecutorImpl(
            coroutineContextProvider = coroutineContextProvider,
            repository = repository
        )
    }

    fun create(): DatabaseStore {
        return object : DatabaseStore,
            Store<DatabaseStore.Intent, DatabaseStore.State, DatabaseStore.Label>
            by storeFactory.create(
                name = "DatabaseStore",
                initialState = DatabaseStore.State(),
                bootstrapper = SimpleBootstrapper(DatabaseStore.Action.SubscribeToDatabase),
                executorFactory = executorFactory,
                reducer = DatabaseReducerImpl()
            ) {}
    }
}
